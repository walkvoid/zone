package com.github.walkvoid.zone.ai.llm;

import tools.jackson.databind.JsonNode;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * LLM 调用客户端，封装OpenAI兼容的Chat Completions API调用
 *
 * @author walkvoid
 */
@Component
public class LLMClient {

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * 发送Chat请求到LLM服务并返回响应内容
     *
     * @param baseUrl      LLM服务Base URL
     * @param apiKey       API Key
     * @param model        模型名称
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return LLM返回的文本内容
     */
    public String chat(String baseUrl, String apiKey, String model,
                       String systemPrompt, String userPrompt) {
        String url = joinUrl(baseUrl, "/chat/completions");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.1);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("LLM request -> {} model={}", url, model);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = JsonUtils.getObjectMapper().readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            log.info("LLM response length={}", content.length());
            return content;
        } catch (Exception e) {
            log.error("LLM call failed: {}", e.getMessage());
            throw new RuntimeException("大模型调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式调用 Chat Completions（stream=true），通过 onDelta 回调逐段内容。
     *
     * @param baseUrl     LLM Base URL（如 https://api.deepseek.com/v1）
     * @param apiKey      API Key
     * @param model       模型名
     * @param messages    OpenAI 兼容 messages
     * @param temperature 温度，可为 null
     * @param onDelta     每个 content delta 回调
     */
    public void streamChat(String baseUrl,
                           String apiKey,
                           String model,
                           List<Map<String, String>> messages,
                           Double temperature,
                           Consumer<String> onDelta) {
        String url = joinUrl(baseUrl, "/chat/completions");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", true);
        body.put("messages", messages);
        if (temperature != null) {
            body.put("temperature", temperature);
        }

        try {
            String json = JsonUtils.getObjectMapper().writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            log.info("LLM stream request -> {} model={}", url, model);
            HttpResponse<InputStream> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String err = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("HTTP " + response.statusCode() + ": " + err);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || !trimmed.startsWith("data:")) {
                        continue;
                    }
                    String data = trimmed.substring(5).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    try {
                        JsonNode root = JsonUtils.getObjectMapper().readTree(data);
                        String delta = root.path("choices").path(0).path("delta").path("content").asText(null);
                        if (delta != null && !delta.isEmpty()) {
                            onDelta.accept(delta);
                        }
                    } catch (Exception parseEx) {
                        log.debug("Skip non-json SSE chunk: {}", data);
                    }
                }
            }
            log.info("LLM stream finished model={}", model);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM stream failed: {}", e.getMessage());
            throw new RuntimeException("大模型流式调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 组装 OpenAI messages：可选前置 systemPrompt + 请求体 messages。
     */
    public static List<Map<String, String>> buildMessages(String systemPrompt,
                                                          List<Map<String, String>> messages) {
        List<Map<String, String>> result = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            result.add(Map.of("role", "system", "content", systemPrompt));
        }
        if (messages != null) {
            for (Map<String, String> m : messages) {
                if (m == null) {
                    continue;
                }
                String role = m.get("role");
                String content = m.get("content");
                if (role == null || content == null) {
                    continue;
                }
                result.add(Map.of("role", role, "content", content));
            }
        }
        return result;
    }

    private static String joinUrl(String baseUrl, String path) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return base + path;
    }
}
