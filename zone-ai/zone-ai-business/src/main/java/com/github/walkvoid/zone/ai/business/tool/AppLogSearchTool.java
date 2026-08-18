package com.github.walkvoid.zone.ai.business.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.zone.ai.business.tool.log.BeeCloudProperties;
import com.github.walkvoid.zone.ai.business.tool.log.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpRetryException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;

/**
 * @author jiangjunqing
 * @date 2026/8/12
 * @description: 应用日志搜索工具。Cookie 过期时自动走 SSO 登录刷新 token。
 */
@Component
public class AppLogSearchTool {

    private static final Logger log = LoggerFactory.getLogger(AppLogSearchTool.class);
    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));

    private final ObjectMapper mapper = new ObjectMapper();
    private final BeeCloudProperties properties;
    private final TokenStore tokenStore;
    private final RestTemplate restTemplate;

    public AppLogSearchTool(BeeCloudProperties properties,
                            TokenStore tokenStore,
                            RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.tokenStore = tokenStore;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .setReadTimeout(Duration.ofSeconds(Math.max(properties.requestTimeoutSeconds(), 30)))
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public boolean hasError(ClientHttpResponse response) throws IOException {
                        return false;
                    }
                })
                .build();
    }

    @Tool(description = "搜索日志平台中的日志。根据搜索关键词（支持traceId、错误信息等）在指定环境、时间范围内搜索日志。")
    public JsonNode beecloudSearchLogs(
            @ToolParam(description = "搜索关键词，可以是 traceId、错误信息片段等", required = true) String searchStr,
            @ToolParam(description = "环境标识，如 dev", required = true) String env,
            @ToolParam(description = "搜索类型，如 devops，默认 devops") String searchType,
            @ToolParam(description = "开始时间（ISO 8601格式，如 2026-08-11T09:02:12Z），未指定时配合 minutesAgo 使用") String gte,
            @ToolParam(description = "结束时间（ISO 8601格式，如 2026-08-12T09:02:12Z），未指定时默认当前时间") String lte,
            @ToolParam(description = "搜索最近多少分钟内的日志，当未指定 gte/lte 时生效，默认 60 分钟") Integer minutesAgo,
            @ToolParam(description = "集群标识，默认 dev-fn-stand") String cluster,
            @ToolParam(description = "最大返回条数，默认 50") Integer maxResults) {
        log.info("beecloudSearchLogs args: searchStr={}, env={}, searchType={}, gte={}, lte={}, minutesAgo={}, cluster={}, maxResults={}",
                searchStr, env, searchType, gte, lte, minutesAgo, cluster, maxResults);

        try {
            if (!properties.isEnabled()) {
                return errorResult("BeeCloud log search is disabled (zone.ai.tool.beecloud.enabled=false).");
            }
            if (searchType == null || searchType.isBlank()) searchType = "devops";
            if (maxResults == null) maxResults = 50;
            if (minutesAgo == null) minutesAgo = 60;
            if (cluster == null || cluster.isBlank()) cluster = "dev-fn-stand";
            if (gte != null && gte.isBlank()) gte = null;
            if (lte != null && lte.isBlank()) lte = null;

            Instant now = Instant.now();
            if (lte == null) lte = ISO_FMT.format(now);
            if (gte == null) gte = ISO_FMT.format(now.minus(Duration.ofMinutes(minutesAgo)));

            String queryString = searchStr.startsWith("\"") ? searchStr : "\"" + searchStr + "\"";

            ObjectNode timeRange = mapper.createObjectNode();
            timeRange.put("format", "strict_date_optional_time");
            timeRange.put("gte", gte);
            timeRange.put("lte", lte);

            ObjectNode dateHistogram = mapper.createObjectNode();
            dateHistogram.put("fixed_interval", "15s");
            dateHistogram.put("field", "@timestamp");
            dateHistogram.put("min_doc_count", 1);
            dateHistogram.put("time_zone", "Asia/Shanghai");

            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("search_type", searchType);
            requestBody.set("time_range", timeRange);
            requestBody.put("query_string", queryString);
            requestBody.set("date_histogram", dateHistogram);
            requestBody.put("size", 10000);
            requestBody.put("order", false);
            requestBody.put("env", env);
            requestBody.put("env_instance", properties.getProject());
            requestBody.put("cluster", cluster);

            String jsonBody = mapper.writeValueAsString(requestBody);
            log.info("beecloudSearchLogs: url={}, searchStr={}, env={}, gte={}, lte={}",
                    properties.searchUrl(), searchStr, env, gte, lte);

            ResponseEntity<String> resp = searchWithAuthRetry(jsonBody);

            String body = resp.getBody();
            if (!resp.getStatusCode().is2xxSuccessful() || body == null) {
                return errorResult("HTTP " + resp.getStatusCode().value()
                        + (body == null ? "" : ": " + truncate(body, 300)));
            }
            if (isAuthFailure(resp)) {
                return errorResult("BeeCloud login succeeded but search still unauthorized. Check account permission.");
            }

            return slimResult(body, maxResults);

        } catch (Exception e) {
            log.error("beecloudSearchLogs failed: {}", e.getMessage(), e);
            return errorResult(e.getMessage());
        }
    }

    private ResponseEntity<String> postSearch(String jsonBody, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.COOKIE, "token=" + token + "; locale=zh-CN");
        return restTemplate.exchange(
                properties.searchUrl(), HttpMethod.POST, new HttpEntity<>(jsonBody, headers), String.class);
    }

    private ResponseEntity<String> searchWithAuthRetry(String jsonBody) {
        String token = tokenStore.resolveToken(null);
        ResponseEntity<String> resp;
        try {
            resp = postSearch(jsonBody, token);
        } catch (RuntimeException ex) {
            if (!isStreamingAuthChallenge(ex)) {
                throw ex;
            }
            log.info("BeeCloud auth challenge before response, force re-login and retry once: {}",
                    rootMessage(ex));
            token = tokenStore.refreshToken();
            resp = postSearch(jsonBody, token);
        }
        if (isAuthFailure(resp)) {
            log.info("BeeCloud token expired or missing, re-login and retry once");
            token = tokenStore.refreshToken();
            resp = postSearch(jsonBody, token);
        }
        return resp;
    }

    private boolean isStreamingAuthChallenge(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Throwable cursor = ex;
        while (cursor != null) {
            if (cursor instanceof HttpRetryException) {
                String msg = cursor.getMessage();
                return msg != null && msg.toLowerCase(Locale.ROOT).contains("server authentication");
            }
            String msg = cursor.getMessage();
            if (cursor instanceof ResourceAccessException
                    && msg != null
                    && msg.toLowerCase(Locale.ROOT).contains("cannot retry due to server authentication")) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private String rootMessage(Throwable ex) {
        Throwable cursor = ex;
        while (cursor != null && cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        return cursor == null ? "" : String.valueOf(cursor.getMessage());
    }

    private boolean isAuthFailure(ResponseEntity<String> resp) {
        int status = resp.getStatusCode().value();
        if (status == 401 || status == 403) {
            return true;
        }
        String body = resp.getBody();
        if (body == null || body.isBlank()) {
            return false;
        }
        String trimmed = body.stripLeading();
        if (trimmed.startsWith("<")) {
            return true;
        }
        if (!trimmed.startsWith("{")) {
            return false;
        }
        try {
            JsonNode node = mapper.readTree(trimmed);
            int code = node.path("code").asInt(0);
            String message = node.path("message").asText("") + " " + node.path("msg").asText("");
            return code == 401 || code == 403
                    || message.contains("未登录")
                    || message.contains("登录过期")
                    || message.contains("unauthorized")
                    || message.contains("Unauthenticated");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从原始 ES 响应中只提取 LLM 关心的字段：
     * timestamp, service, hostname, message
     */
    private JsonNode slimResult(String rawJson, int maxResults) {
        try {
            JsonNode root = mapper.readTree(rawJson);

            int code = root.path("code").asInt(-1);
            if (code != 0) {
                ObjectNode err = mapper.createObjectNode();
                err.put("success", false);
                err.put("error", root.path("message").asText("unknown"));
                return err;
            }

            JsonNode hits = root.path("data").path("hits").path("hits");
            long total = root.path("data").path("hits").path("total").path("value").asLong();

            ArrayNode rows = mapper.createArrayNode();
            int count = 0;
            for (JsonNode hit : hits) {
                if (count >= maxResults) break;

                JsonNode src = hit.path("_source");
                ObjectNode row = mapper.createObjectNode();
                row.put("timestamp", src.path("@timestamp").asText());
                row.put("service", src.path("service").asText());
                row.put("hostname", src.path("hostname").asText());
                row.put("message", src.path("message").asText());
                rows.add(row);
                count++;
            }

            ObjectNode result = mapper.createObjectNode();
            result.put("success", true);
            result.put("total", total);
            result.put("returned", count);
            result.set("rows", rows);
            return result;

        } catch (Exception e) {
            log.error("Parse beecloud response failed", e);
            return errorResult("Parse error: " + e.getMessage());
        }
    }

    private JsonNode errorResult(String msg) {
        ObjectNode err = mapper.createObjectNode();
        err.put("success", false);
        err.put("error", msg);
        return err;
    }

    private static String truncate(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "...";
    }
}
