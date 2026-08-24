package com.github.walkvoid.zone.ai.agent;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 机器人可挂载的工具编码，对应 {@code ai_bot_config.tool_codes}。
 * <p>
 * {@link #ready()} 为 false 的条目仅出现在工具目录中，供 Prompt 运行后处理等场景预选，实现后改为 true 并注册 Bean。
 */
public enum AgentToolCode {
    LOG("log", "日志搜索", "搜索应用/平台日志（traceId、报错等）", true),
    SQL("sql", "业务查询", "只读查询供应链业务库（命名查询 / SELECT）", true),
    REPO_READ("repo_read", "读代码", "在沙箱白名单内搜索、阅读源码", true),
    REPO_CHANGE("repo_change", "改代码", "按策略写入补丁或直接改源文件", true),
    KNOWLEDGE("knowledge", "知识检索", "检索业务文档 / 代码地图知识库", true),
    /** 将结果写入 zone-ai 主库白名单表（默认 prompt_result_archive） */
    DB_INSERT("db_insert", "数据库插入", "将 Prompt 运行结果写入白名单表（默认归档表）", true),
    /** 将结果上传到 MinIO */
    FILE_UPLOAD("file_upload", "文件上传", "将 Prompt 运行结果保存并上传到 MinIO 文件管理", true);

    private final String code;
    private final String label;
    private final String description;
    private final boolean ready;

    AgentToolCode(String code, String label, String description, boolean ready) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.ready = ready;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    /** 是否已实现并可挂载到 ChatClient */
    public boolean ready() {
        return ready;
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
     * 解析逗号分隔编码。空配置时返回只读默认集（含 knowledge，不含改代码与未实现工具）。
     */
    public static List<AgentToolCode> parse(String csv) {
        if (!StringUtils.hasText(csv)) {
            return List.of(LOG, SQL, REPO_READ, KNOWLEDGE);
        }
        Set<AgentToolCode> result = new LinkedHashSet<>();
        for (String part : csv.split("[,;\\s]+")) {
            AgentToolCode code = fromCode(part);
            if (code != null && code.ready()) {
                result.add(code);
            }
        }
        if (result.isEmpty()) {
            return List.of(LOG, SQL, REPO_READ, KNOWLEDGE);
        }
        return new ArrayList<>(result);
    }
}
