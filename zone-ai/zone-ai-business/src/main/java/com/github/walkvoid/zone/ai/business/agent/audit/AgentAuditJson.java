package com.github.walkvoid.zone.ai.business.agent.audit;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

final class AgentAuditJson {

    private static final int SUMMARY_MAX = 240;

    private AgentAuditJson() {
    }

    static String truncate(String text, int maxBytes) {
        if (!StringUtils.hasText(text)) {
            return text == null ? null : "";
        }
        int limit = Math.max(256, maxBytes);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= limit) {
            return text;
        }
        int keep = Math.max(0, limit - 3);
        String cut = new String(bytes, 0, keep, StandardCharsets.UTF_8);
        if (!cut.isEmpty() && Character.isHighSurrogate(cut.charAt(cut.length() - 1))) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "…";
    }

    static String summarize(JsonNode node, String toolName, boolean success, String error) {
        if (!success && StringUtils.hasText(error)) {
            return truncate(("失败 " + toolName + ": " + error).replace('\n', ' '), SUMMARY_MAX);
        }
        if (node == null || node.isMissingNode() || node.isNull()) {
            return success ? toolName + " 完成" : toolName + " 失败";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(toolName == null ? "tool" : toolName);
        if (node.has("success")) {
            sb.append(node.path("success").asBoolean(true) ? " 成功" : " 失败");
        } else {
            sb.append(success ? " 成功" : " 失败");
        }
        appendIfPresent(sb, node, "returned", "条");
        appendIfPresent(sb, node, "hits", null);
        appendIfPresent(sb, node, "rowCount", "行");
        appendIfPresent(sb, node, "rows", null);
        if (node.has("error") && StringUtils.hasText(node.path("error").asText())) {
            sb.append("：").append(node.path("error").asText());
        }
        if (node.has("queryCode")) {
            sb.append(" query=").append(node.path("queryCode").asText());
        }
        if (node.has("path")) {
            sb.append(" ").append(node.path("path").asText());
        }
        return truncate(sb.toString(), SUMMARY_MAX);
    }

    private static void appendIfPresent(StringBuilder sb, JsonNode node, String field, String suffix) {
        JsonNode value = node.get(field);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return;
        }
        if (value.isArray()) {
            sb.append(' ').append(field).append('=').append(value.size());
            return;
        }
        if (value.isNumber() || value.isTextual()) {
            sb.append(' ').append(field).append('=').append(value.asText());
            if (StringUtils.hasText(suffix)) {
                sb.append(suffix);
            }
        }
    }
}
