package com.github.walkvoid.zone.ai.business.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.utils.JsonNodeUtils;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.business.tool.sql.NamedSqlBinder;
import com.github.walkvoid.zone.ai.business.tool.sql.NamedSqlQuery;
import com.github.walkvoid.zone.ai.business.tool.sql.NamedSqlQueryCatalog;
import com.github.walkvoid.zone.ai.business.tool.sql.SqlGuard;
import com.github.walkvoid.zone.ai.business.tool.sql.SqlQuerySupport;
import com.github.walkvoid.zone.ai.business.tool.sql.SqlQueryToolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 供应链业务 MySQL 只读查询。同时给 ChatClient 和 MCP Server 使用。
 */
@Component
public class SqlQueryTool {

    private static final Logger log = LoggerFactory.getLogger(SqlQueryTool.class);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SqlQuerySupport support;
    private final NamedSqlQueryCatalog catalog;
    public SqlQueryTool(SqlQuerySupport support, NamedSqlQueryCatalog catalog) {
        this.support = support;
        this.catalog = catalog;
    }

    @Tool(description = "列出常用命名查询目录。查用户/合同/客户/凭证/兑付/网关日志等优先用本目录的 queryCode，再调用 runNamedQuery。")
    public JsonNode listNamedQueries() {
        ArrayNode queries = JsonUtils.getObjectMapper().createArrayNode();
        for (NamedSqlQuery q : catalog.all()) {
            ObjectNode item = JsonUtils.getObjectMapper().createObjectNode();
            item.put("code", q.code());
            item.put("category", q.category());
            item.put("description", q.description());
            ArrayNode by = JsonUtils.getObjectMapper().createArrayNode();
            q.byOptions().forEach(by::add);
            item.set("by", by);
            queries.add(item);
        }
        ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
        result.put("success", true);
        result.set("queries", queries);
        return result;
    }

    @Tool(description = "执行一条常用命名查询。先 listNamedQueries 选 queryCode。"
            + "paramsJson 为 JSON：常用字段 by、value；网关日志还需要 interfaceCode，按渠道时再加 channel。"
            + "例如 {\"by\":\"cust_no\",\"value\":\"CUST202503070247\"}。禁止手写 SQL。")
    public JsonNode runNamedQuery(
            @ToolParam(description = "命名查询编码，来自 listNamedQueries 的 code", required = true) String queryCode,
            @ToolParam(description = "JSON 参数，如 {\"by\":\"id\",\"value\":\"123\"}。无需参数的查询可传 {}") String paramsJson,
            @ToolParam(description = "最大返回行数，默认配置值，硬上限 200") Integer maxRows) {
        try {
            JsonNode notReady = notReady();
            if (notReady != null) {
                return notReady;
            }
            if (!StringUtils.hasText(queryCode)) {
                return error("queryCode is required");
            }
            JsonNode params = parseParams(paramsJson);
            int limit = capMaxRows(maxRows);
            NamedSqlQuery query = catalog.get(queryCode.trim());
            NamedSqlBinder.BoundQuery bound = NamedSqlBinder.bind(
                    query, params, limit, support.properties().allowedTableSet());
            log.info("runNamedQuery code={}, sql={}", query.code(), bound.sql());
            return query(bound.sql(), bound.args(), limit);
        } catch (Exception e) {
            return fail("runNamedQuery", e);
        }
    }

    @Tool(description = "列出当前允许查询的供应链业务表。查库前先调用本方法确认表名。")
    public JsonNode listAllowedTables() {
        try {
            JsonNode notReady = notReady();
            if (notReady != null) {
                return notReady;
            }
            SqlQueryToolProperties props = support.properties();
            Set<String> allowed = props.allowedTableSet();
            ArrayNode tables = JsonUtils.getObjectMapper().createArrayNode();
            if (allowed.isEmpty()) {
                return okTables(tables);
            }
            List<String> qualified = new ArrayList<>();
            List<String> simple = new ArrayList<>();
            for (String name : allowed) {
                if (name.contains(".")) {
                    qualified.add(name);
                } else {
                    simple.add(name);
                }
            }
            if (!qualified.isEmpty()) {
                String placeholders = String.join(",", qualified.stream().map(t -> "?").toList());
                String sql = "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
                        + "WHERE CONCAT(TABLE_SCHEMA, '.', TABLE_NAME) IN (" + placeholders + ") "
                        + "ORDER BY TABLE_SCHEMA, TABLE_NAME";
                support.jdbc().query(sql, (rs, rowNum) -> {
                    ObjectNode row = JsonUtils.getObjectMapper().createObjectNode();
                    row.put("table", rs.getString("TABLE_SCHEMA") + "." + rs.getString("TABLE_NAME"));
                    row.put("comment", rs.getString("TABLE_COMMENT"));
                    return row;
                }, qualified.toArray()).forEach(tables::add);
            }
            if (!simple.isEmpty()) {
                String placeholders = String.join(",", simple.stream().map(t -> "?").toList());
                String sql = "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN (" + placeholders + ") "
                        + "ORDER BY TABLE_NAME";
                support.jdbc().query(sql, (rs, rowNum) -> {
                    ObjectNode row = JsonUtils.getObjectMapper().createObjectNode();
                    row.put("table", rs.getString("TABLE_NAME"));
                    row.put("comment", rs.getString("TABLE_COMMENT"));
                    return row;
                }, simple.toArray()).forEach(tables::add);
            }
            Set<String> found = new java.util.HashSet<>();
            tables.forEach(n -> found.add(JsonNodeUtils.asText(n, "table").toLowerCase(Locale.ROOT)));
            for (String name : allowed) {
                if (!found.contains(name)) {
                    ObjectNode row = JsonUtils.getObjectMapper().createObjectNode();
                    row.put("table", name);
                    row.put("comment", "configured but not found in current database");
                    tables.add(row);
                }
            }
            return okTables(tables);
        } catch (Exception e) {
            return fail("listAllowedTables", e);
        }
    }

    @Tool(description = "查看白名单表的字段结构（列名、类型、注释）。参数 table 必须是 listAllowedTables 返回的表名。")
    public JsonNode describeTable(@ToolParam(description = "表名，必须在允许列表中", required = true) String table) {
        try {
            JsonNode notReady = notReady();
            if (notReady != null) {
                return notReady;
            }
            String tableName = SqlGuard.requireAllowedTable(table, support.properties().allowedTableSet());
            String schema = null;
            String plain = tableName;
            int dot = tableName.indexOf('.');
            if (dot > 0) {
                schema = tableName.substring(0, dot);
                plain = tableName.substring(dot + 1);
            }
            ArrayNode columns = JsonUtils.getObjectMapper().createArrayNode();
            List<ObjectNode> foundCols;
            if (schema != null) {
                String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT "
                        + "FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? "
                        + "ORDER BY ORDINAL_POSITION";
                foundCols = support.jdbc().query(sql, (rs, rowNum) -> toColumn(rs), schema, plain);
            } else {
                String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT "
                        + "FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? "
                        + "ORDER BY ORDINAL_POSITION";
                foundCols = support.jdbc().query(sql, (rs, rowNum) -> toColumn(rs), plain);
            }
            foundCols.forEach(columns::add);
            if (columns.isEmpty()) {
                return error("table not found in current database: " + tableName);
            }
            ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
            result.put("success", true);
            result.put("table", tableName);
            result.set("columns", columns);
            return result;
        } catch (Exception e) {
            return fail("describeTable", e);
        }
    }

    @Tool(description = "按某个字段等值查询白名单表，使用参数绑定，适合按单号/ID/状态精确查找。")
    public JsonNode queryByColumn(@ToolParam(description = "表名，必须在允许列表中", required = true) String table,
                                  @ToolParam(description = "等值匹配的列名", required = true) String column,
                                  @ToolParam(description = "等值匹配的值", required = true) String value,
                                  @ToolParam(description = "最大返回行数，默认配置值，硬上限 200") Integer maxRows) {
        try {
            JsonNode notReady = notReady();
            if (notReady != null) {
                return notReady;
            }
            if (!StringUtils.hasText(value)) {
                return error("value is required");
            }
            String tableName = SqlGuard.requireAllowedTable(table, support.properties().allowedTableSet());
            String columnName = SqlGuard.requireIdent(column, "column");
            int limit = capMaxRows(maxRows);
            String sql = "SELECT * FROM " + SqlGuard.quoteQualified(tableName)
                    + " WHERE `" + columnName + "` = ? LIMIT " + limit;
            log.info("queryByColumn table={}, column={}, limit={}", tableName, columnName, limit);
            return query(sql, new Object[]{value}, limit);
        } catch (Exception e) {
            return fail("queryByColumn", e);
        }
    }


    @Tool(description = "执行一条只读 SELECT。仅允许单条 SELECT/WITH，表必须在白名单。"
            + "常用业务查询请优先 runNamedQuery，不要把命名查询改写成 SQL。")
    public JsonNode selectSql(@ToolParam(description = "单条 SELECT 语句，不要分号、注释", required = true) String sql,
                              @ToolParam(description = "最大返回行数，默认配置值，硬上限 200") Integer maxRows) {
        try {
            JsonNode notReady = notReady();
            if (notReady != null) {
                return notReady;
            }
            int limit = capMaxRows(maxRows);
            String guarded = SqlGuard.guardSelect(sql, support.properties().allowedTableSet(), limit);
            log.info("selectSql guarded={}", guarded);
            return query(guarded, new Object[0], limit);
        } catch (Exception e) {
            return fail("selectSql", e);
        }
    }

    private JsonNode query(String sql, Object[] args, int limit) {
        JdbcTemplate jdbc = support.jdbc();
        List<ObjectNode> rows = jdbc.query(sql, rs -> {
            List<ObjectNode> list = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            int count = 0;
            while (rs.next()) {
                if (count >= limit) {
                    break;
                }
                list.add(toRow(rs, meta, colCount));
                count++;
            }
            return list;
        }, args);

        ArrayNode array = JsonUtils.getObjectMapper().createArrayNode();
        if (rows != null) {
            rows.forEach(array::add);
        }
        ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
        result.put("success", true);
        result.put("returned", array.size());
        result.put("truncated", array.size() >= limit);
        result.set("rows", array);
        return result;
    }

    private ObjectNode toRow(ResultSet rs, ResultSetMetaData meta, int colCount) throws java.sql.SQLException {
        ObjectNode row = JsonUtils.getObjectMapper().createObjectNode();
        Set<String> sensitive = support.properties().sensitiveColumnSet();
        int maxChars = Math.max(32, support.properties().getMaxCellChars());
        for (int i = 1; i <= colCount; i++) {
            String label = meta.getColumnLabel(i);
            if (isSensitive(label, sensitive)) {
                Object raw = rs.getObject(i);
                row.put(label, raw == null ? null : "***");
                continue;
            }
            putCell(row, label, rs, i, meta.getColumnType(i), maxChars);
        }
        return row;
    }

    private void putCell(ObjectNode row, String label, ResultSet rs, int index, int type, int maxChars)
            throws java.sql.SQLException {
        if (type == Types.BLOB || type == Types.BINARY || type == Types.VARBINARY || type == Types.LONGVARBINARY) {
            byte[] bytes = rs.getBytes(index);
            row.put(label, bytes == null ? null : "<binary " + bytes.length + " bytes>");
            return;
        }
        Object value = rs.getObject(index);
        if (value == null) {
            row.putNull(label);
            return;
        }
        switch (value) {
            case Timestamp ts -> row.put(label, ts.toLocalDateTime().format(TS_FMT));
            case Date d -> row.put(label, d.toLocalDate().toString());
            case Time t -> row.put(label, t.toLocalTime().toString());
            case BigDecimal bd -> row.put(label, bd.toPlainString());
            case Boolean b -> row.put(label, b);
            case Number n -> row.put(label, n.toString());
            case byte[] bytes -> row.put(label, "<binary " + bytes.length + " bytes>");
            default -> {
                String text = String.valueOf(value);
                if (text.length() > maxChars) {
                    text = text.substring(0, maxChars) + "...(truncated)";
                }
                row.put(label, text);
            }
        }
    }

    private static boolean isSensitive(String column, Set<String> sensitive) {
        if (column == null || sensitive.isEmpty()) {
            return false;
        }
        String lower = column.toLowerCase(Locale.ROOT);
        for (String token : sensitive) {
            if (lower.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private int capMaxRows(Integer requested) {
        SqlQueryToolProperties props = support.properties();
        int def = Math.max(1, props.getMaxRows());
        int hard = Math.max(def, props.getHardMaxRows());
        int n = requested == null ? def : requested;
        if (n < 1) {
            n = def;
        }
        return Math.min(n, hard);
    }

    private JsonNode notReady() {
        if (support.isReady()) {
            return null;
        }
        SqlQueryToolProperties props = support.properties();
        return error("SQL query tool is not connected. enabled=" + props.isEnabled()
                + ", urlConfigured=" + StringUtils.hasText(props.getUrl())
                + ". Uncomment zone.ai.tool.sql.username / password in application.properties if needed.");
    }

    private ObjectNode okTables(ArrayNode tables) {
        ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
        result.put("success", true);
        result.set("tables", tables);
        return result;
    }

    private ObjectNode toColumn(ResultSet rs) throws java.sql.SQLException {
        ObjectNode col = JsonUtils.getObjectMapper().createObjectNode();
        col.put("name", rs.getString("COLUMN_NAME"));
        col.put("type", rs.getString("DATA_TYPE"));
        col.put("nullable", "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
        col.put("key", rs.getString("COLUMN_KEY"));
        col.put("comment", rs.getString("COLUMN_COMMENT"));
        return col;
    }

    private JsonNode parseParams(String paramsJson) throws Exception {
        if (!StringUtils.hasText(paramsJson) || "{}".equals(paramsJson.trim())) {
            return JsonUtils.getObjectMapper().createObjectNode();
        }
        return JsonUtils.getObjectMapper().readTree(paramsJson);
    }

    private JsonNode fail(String action, Exception e) {
        log.warn("{} failed: {}", action, e.getMessage());
        return error(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
    }

    private ObjectNode error(String msg) {
        ObjectNode err = JsonUtils.getObjectMapper().createObjectNode();
        err.put("success", false);
        err.put("error", msg);
        return err;
    }
}
