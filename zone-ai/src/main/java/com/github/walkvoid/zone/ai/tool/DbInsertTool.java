package com.github.walkvoid.zone.ai.tool;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.tool.sql.SqlGuard;
import com.github.walkvoid.zone.ai.tool.write.DbInsertToolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 白名单表写入。默认把 Prompt 运行结果写入 {@code prompt_result_archive}（zone-ai 主库）。
 * 禁止任意 SQL；仅支持参数化 INSERT。与只读 SqlQueryTool（业务库）分离。
 */
@Component
public class DbInsertTool {

    private static final Logger log = LoggerFactory.getLogger(DbInsertTool.class);
    private static final String ARCHIVE_TABLE = "prompt_result_archive";
    private static final Set<String> BLOCKED_COLUMNS = Set.of(
            "password", "secret", "token", "api_key", "access_key", "secret_key");

    private final JdbcTemplate jdbc;
    private final DbInsertToolProperties properties;

    public DbInsertTool(DataSource dataSource, DbInsertToolProperties properties) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.properties = properties;
    }

    @Tool(description = "列出当前允许 INSERT 的表。写入前先调用确认表名。"
            + "默认包含 prompt_result_archive（Prompt 运行结果归档）。")
    public JsonNode listInsertableTables() {
        if (!properties.isEnabled()) {
            return error("db_insert tool is disabled (zone.ai.tool.db-insert.enabled=false)");
        }
        ArrayNode tables = JsonUtils.getObjectMapper().createArrayNode();
        for (String t : properties.allowedTableSet()) {
            tables.add(t);
        }
        ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
        result.put("success", true);
        result.set("tables", tables);
        result.put("defaultArchiveTable", ARCHIVE_TABLE);
        return result;
    }

    @Tool(description = "将一段文本结果写入默认归档表 prompt_result_archive。"
            + "适合 Prompt 运行后处理后落库。返回生成的 id。"
            + "bizCode 默认 prompt_run；metaJson 可选，存额外 JSON 元数据。")
    public JsonNode saveResult(
            @ToolParam(description = "结果正文", required = true) String content,
            @ToolParam(description = "标题，可空") String title,
            @ToolParam(description = "业务编码，缺省 prompt_run") String bizCode,
            @ToolParam(description = "可选元数据 JSON 字符串") String metaJson) {
        try {
            if (!properties.isEnabled()) {
                return error("db_insert tool is disabled");
            }
            ensureArchiveAllowed();
            if (!StringUtils.hasText(content)) {
                return error("content is required");
            }
            if (content.length() > properties.getMaxContentChars()) {
                return error("content too large, maxChars=" + properties.getMaxContentChars());
            }
            String biz = StringUtils.hasText(bizCode) ? bizCode.trim() : "prompt_run";
            String titleVal = StringUtils.hasText(title) ? title.trim() : null;
            String meta = StringUtils.hasText(metaJson) ? metaJson.trim() : null;
            if (meta != null) {
                try {
                    JsonUtils.getObjectMapper().readTree(meta);
                } catch (Exception e) {
                    return error("metaJson is not valid JSON: " + e.getMessage());
                }
            }

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO `prompt_result_archive` (`biz_code`,`title`,`content`,`meta_json`,`source_tool`) "
                                + "VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, biz);
                ps.setString(2, titleVal);
                ps.setString(3, content);
                ps.setString(4, meta);
                ps.setString(5, "db_insert");
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
            result.put("success", true);
            result.put("table", ARCHIVE_TABLE);
            if (key != null) {
                result.put("id", key.longValue());
            }
            result.put("bizCode", biz);
            result.put("contentChars", content.length());
            log.info("DbInsertTool.saveResult id={}, biz={}, chars={}", key, biz, content.length());
            return result;
        } catch (Exception e) {
            return fail("saveResult", e);
        }
    }

    @Tool(description = "向白名单表插入一行。table 必须在 listInsertableTables 中；"
            + "rowJson 为列名到值的 JSON 对象，例如 {\"biz_code\":\"demo\",\"title\":\"t\",\"content\":\"...\"}。"
            + "列名仅允许字母数字下划线；禁止 password/secret 等敏感列；禁止手写 SQL。")
    public JsonNode insertRow(
            @ToolParam(description = "表名（可 schema.table）", required = true) String table,
            @ToolParam(description = "行数据 JSON 对象", required = true) String rowJson) {
        try {
            if (!properties.isEnabled()) {
                return error("db_insert tool is disabled");
            }
            String canonical = SqlGuard.requireAllowedTable(table, properties.allowedTableSet());
            if (!StringUtils.hasText(rowJson)) {
                return error("rowJson is required");
            }
            JsonNode root = JsonUtils.getObjectMapper().readTree(rowJson);
            if (root == null || !root.isObject() || root.isEmpty()) {
                return error("rowJson must be a non-empty JSON object");
            }

            Map<String, Object> columns = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> entry : root.properties()) {
                String col = SqlGuard.requireIdent(entry.getKey(), "column").toLowerCase(Locale.ROOT);
                if (isBlockedColumn(col)) {
                    return error("column is blocked: " + col);
                }
                if ("id".equals(col) || "deleted".equals(col)) {
                    continue;
                }
                columns.put(col, jsonToSqlValue(entry.getValue()));
            }
            if (columns.isEmpty()) {
                return error("no insertable columns in rowJson");
            }
            if (columns.size() > properties.getMaxColumns()) {
                return error("too many columns, max=" + properties.getMaxColumns());
            }

            StringBuilder sql = new StringBuilder("INSERT INTO ")
                    .append(SqlGuard.quoteQualified(canonical))
                    .append(" (");
            StringBuilder placeholders = new StringBuilder();
            List<Object> args = new ArrayList<>();
            boolean first = true;
            for (Map.Entry<String, Object> e : columns.entrySet()) {
                if (!first) {
                    sql.append(',');
                    placeholders.append(',');
                }
                first = false;
                sql.append('`').append(e.getKey()).append('`');
                placeholders.append('?');
                args.add(e.getValue());
            }
            sql.append(") VALUES (").append(placeholders).append(')');

            KeyHolder keyHolder = new GeneratedKeyHolder();
            String finalSql = sql.toString();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < args.size(); i++) {
                    ps.setObject(i + 1, args.get(i));
                }
                return ps;
            }, keyHolder);

            ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
            result.put("success", true);
            result.put("table", canonical);
            result.put("columns", columns.size());
            Number key = keyHolder.getKey();
            if (key != null) {
                result.put("id", key.longValue());
            }
            log.info("DbInsertTool.insertRow table={}, columns={}, id={}", canonical, columns.size(), key);
            return result;
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return fail("insertRow", e);
        }
    }

    private void ensureArchiveAllowed() {
        if (!properties.allowedTableSet().contains(ARCHIVE_TABLE)) {
            throw new IllegalStateException(
                    "prompt_result_archive is not in zone.ai.tool.db-insert.allowed-tables");
        }
    }

    private static boolean isBlockedColumn(String col) {
        if (BLOCKED_COLUMNS.contains(col)) {
            return true;
        }
        for (String blocked : BLOCKED_COLUMNS) {
            if (col.contains(blocked)) {
                return true;
            }
        }
        return false;
    }

    private static Object jsonToSqlValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isIntegralNumber()) {
            return node.longValue();
        }
        if (node.isFloatingPointNumber()) {
            return node.doubleValue();
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }

    private ObjectNode error(String message) {
        ObjectNode node = JsonUtils.getObjectMapper().createObjectNode();
        node.put("success", false);
        node.put("message", message);
        return node;
    }

    private ObjectNode fail(String action, Exception e) {
        log.warn("DbInsertTool {} failed: {}", action, e.getMessage());
        return error(action + " failed: " + e.getMessage());
    }
}
