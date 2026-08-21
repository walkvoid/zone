package com.github.walkvoid.zone.ai.agent;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 机器人可挂载的工具编码，对应 {@code ai_bot_config.tool_codes}。
 */
public enum AgentToolCode {
    LOG("log"),
    SQL("sql"),
    REPO_READ("repo_read"),
    REPO_CHANGE("repo_change");

    private final String code;

    AgentToolCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static AgentToolCode fromCode(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        for (AgentToolCode value : values()) {
            if (value.code.equals(normalized)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 解析逗号分隔编码。空配置时返回只读默认集（不含改代码）。
     */
    public static List<AgentToolCode> parse(String csv) {
        if (!StringUtils.hasText(csv)) {
            return List.of(LOG, SQL, REPO_READ);
        }
        Set<AgentToolCode> result = new LinkedHashSet<>();
        for (String part : csv.split("[,;\\s]+")) {
            AgentToolCode code = fromCode(part);
            if (code != null) {
                result.add(code);
            }
        }
        if (result.isEmpty()) {
            return List.of(LOG, SQL, REPO_READ);
        }
        return new ArrayList<>(result);
    }
}
