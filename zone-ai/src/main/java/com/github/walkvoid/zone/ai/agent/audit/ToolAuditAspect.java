package com.github.walkvoid.zone.ai.agent.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.agent.AgentAuditProperties;
import com.github.walkvoid.zone.ai.agent.AgentTurnContext;
import com.github.walkvoid.zone.ai.agent.CodeChangeTurnContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 拦截 {@code @Tool}：热路径只截断 JSON 并入队，不写库。
 */
@Aspect
@Component
public class ToolAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(ToolAuditAspect.class);

    private final AgentAuditQueue queue;
    private final AgentAuditProperties properties;

    public ToolAuditAspect(AgentAuditQueue queue,
                           AgentAuditProperties properties) {
        this.queue = queue;
        this.properties = properties;
    }

    @Around("@annotation(tool)")
    public Object around(ProceedingJoinPoint pjp, Tool tool) throws Throwable {
        AgentTurnContext.State state = AgentTurnContext.currentState();
        if (state == null || !properties.isEnabled()) {
            return pjp.proceed();
        }
        long start = System.nanoTime();
        String toolName = pjp.getSignature().getName();
        String toolCode = toolCodeOf(pjp.getTarget());
        String requestJson = snapshotArgs(pjp);
        try {
            Object result = pjp.proceed();
            offerTool(state, toolCode, toolName, requestJson, result, true, start, null);
            return result;
        } catch (Throwable error) {
            offerTool(state, toolCode, toolName, requestJson, null, false, start, error.getMessage());
            throw error;
        }
    }

    private void offerTool(AgentTurnContext.State state,
                           String toolCode,
                           String toolName,
                           String requestJson,
                           Object result,
                           boolean success,
                           long startNanos,
                           String error) {
        try {
            CodeChangeTurnContext.Turn turn = state.turn;
            JsonNode node = toNode(result);
            String responseJson = AgentAuditJson.truncate(toJson(result), properties.normalizedMaxJsonBytes());
            String summary = AgentAuditJson.summarize(node, toolName, success, error);
            long durationMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
            queue.offer(AgentAuditEvent.tool(
                    turn,
                    state.nextSeq(),
                    toolCode,
                    toolName,
                    requestJson,
                    responseJson,
                    summary,
                    success,
                    durationMs,
                    AgentAuditJson.truncate(error, 512)));
        } catch (Exception e) {
            log.warn("enqueue tool audit failed tool={}: {}", toolName, e.getMessage());
        }
    }

    private String snapshotArgs(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = pjp.getArgs();
            Map<String, Object> body = new LinkedHashMap<>();
            if (names != null) {
                for (int i = 0; i < names.length && i < args.length; i++) {
                    String name = names[i];
                    if (isSensitive(name)) {
                        body.put(name, "******");
                    } else {
                        body.put(name, args[i]);
                    }
                }
            }
            return AgentAuditJson.truncate(JsonUtils.getObjectMapper().writeValueAsString(body), properties.normalizedMaxJsonBytes());
        } catch (Exception e) {
            return "{}";
        }
    }

    private JsonNode toNode(Object result) {
        if (result instanceof JsonNode node) {
            return node;
        }
        if (result == null) {
            return null;
        }
        try {
            return JsonUtils.getObjectMapper().valueToTree(result);
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof CharSequence) {
            return result.toString();
        }
        try {
            return JsonUtils.getObjectMapper().writeValueAsString(result);
        } catch (Exception e) {
            return String.valueOf(result);
        }
    }

    static String toolCodeOf(Object target) {
        if (target == null) {
            return "unknown";
        }
        String simple = target.getClass().getSimpleName();
        if (simple.contains("$$")) {
            simple = simple.substring(0, simple.indexOf("$$"));
        }
        String lower = simple.toLowerCase(Locale.ROOT);
        if (lower.contains("log")) {
            return "log";
        }
        if (lower.contains("sql")) {
            return "sql";
        }
        if (lower.contains("change")) {
            return "repo_change";
        }
        if (lower.contains("repo") || lower.contains("read")) {
            return "repo_read";
        }
        return "unknown";
    }

    private static boolean isSensitive(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("secret")
                || lower.contains("password")
                || lower.contains("token")
                || lower.contains("apikey")
                || lower.contains("api_key");
    }
}
