package com.github.walkvoid.zone.ai.business.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * @author jiangjunqing
 * @date 2026/8/12
 * @description:
 * @version:
 */
@Component
public class CodeAssistantTool {

    private static final Logger log = LoggerFactory.getLogger(CodeAssistantTool.class);
    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String TENANT_ID = "8b434d97-87ab-49f9-a82d-8d3c82df6d5e";
    private static final String PROJECT = "jinkoscf";
    private static final String SEARCH_URL =
            "https://beecloud.llschain.com/beelog/api/v1/tenants/" + TENANT_ID
                    + "/applications/search?project=" + PROJECT;

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
        System.out.println("================开始调用beecloudSearchLogs==================");
        log.info("beecloudSearchLogs args: searchStr={}, env={}, searchType={}, gte={}, lte={}, minutesAgo={}, cluster={}, maxResults={}",
                searchStr, env, searchType, gte, lte, minutesAgo, cluster, maxResults);

        try {
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
            requestBody.put("env_instance", PROJECT);
            requestBody.put("cluster", cluster);

            String jsonBody = mapper.writeValueAsString(requestBody);
            System.out.println("==========调用beecloudSearchLogs,url:" + SEARCH_URL);
            System.out.println("==========调用beecloudSearchLogs,body:" + jsonBody);
            log.info("beecloudSearchLogs: searchStr={}, env={}, gte={}, lte={}", searchStr, env, gte, lte);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Cookie","token=eyJhbGciOiJSUzI1NiIsImtpZCI6IjhlN2Y3NjU0LWUzZGEtNGY4OS1hZTZlLTdkN2RiMjY0NzA4YiJ9.eyJpc3MiOiJodHRwczovL2JlZWNsb3VkLmxsc2NoYWluLmNvbS91NS9hcGkvdjEiLCJzdWIiOiJlYTc1MzNmOS1hNDU3LTQyOGUtYTBlNS1iMTFjYWIwMGZlM2EiLCJleHAiOjE3ODY2MzU4MDAsImlhdCI6MTc4NjU4NTQwMCwidGVuYW50X2lkIjoiOGI0MzRkOTctODdhYi00OWY5LWE4MmQtOGQzYzgyZGY2ZDVlIiwidGVuYW50X25hbWUiOiJsbHMiLCJ1c2VyX2lkIjoiZWE3NTMzZjktYTQ1Ny00MjhlLWEwZTUtYjExY2FiMDBmZTNhIiwidXNlcm5hbWUiOiJqaWFuZ2p1bnFpbmcifQ.Fq_KydNzeFlUVRoer8qtteZIncjCLtKR1g6FpUlCx1l8g1X_fGm-zRL5xM-L4o5tHQumjTFYSXsHZWRZn5LXO12m_Qfdrr1nFVqAuV5WGfAPced5g-QDZqin1nOhVyKb27G9Cs9jTv37nq2wGA7OIlN4S8QcV37cYdcvDCaNEiaMaJ_UaSlvebazJKxtwppBTvs9e4gHAMhUiLkrcVPQabotmEa7JcUcZabkO79hjIW8RLB_YPjnhica80zzPFRri7JEOL9MMnNsCxVxjrHOIbqgguCG-oH-xxs-vYQd617K9u_eAQ19_j1PCwTxvQ0FV6DCEGhBVA6CYENkPCpcRA; locale=zh-CN");
            RestTemplate restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(10))
                    .setReadTimeout(Duration.ofSeconds(30))
                    .build();
            ResponseEntity<String> resp = restTemplate.exchange(
                    SEARCH_URL, HttpMethod.POST, new HttpEntity<>(jsonBody, headers), String.class);
            String body = resp.getBody();
            System.out.println("==========调用beecloudSearchLogs,返回:" + body);

            if (!resp.getStatusCode().is2xxSuccessful() || body == null) {
                return errorResult("HTTP " + resp.getStatusCode());
            }
            if (body.stripLeading().startsWith("<")) {
                return errorResult("BeeCloud returned HTML (login page). Cookie/token is missing or expired.");
            }

            return slimResult(body, maxResults);

        } catch (Exception e) {
            log.error("beecloudSearchLogs failed: {}", e.getMessage(), e);
            return errorResult(e.getMessage());
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
}
