package com.github.walkvoid.zone.ai.business.tool.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.walkvoid.wvframework.utils.JsonNodeUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 把命名查询 + JSON 参数绑成可执行的参数化 SQL。
 */
public final class NamedSqlBinder {

    public static final int MAX_IN_SIZE = 20;

    public record BoundQuery(String sql, Object[] args) {
    }

    private NamedSqlBinder() {
    }

    public static BoundQuery bind(NamedSqlQuery query, JsonNode params, int maxRows, Set<String> allowedTables) {
        JsonNode node = JsonNodeUtils.isAbsent(params) ? null : params;
        String by = text(node, "by");
        NamedSqlQuery.Variant variant = pickVariant(query, by);
        for (String table : variant.tables()) {
            SqlGuard.requireAllowedTable(table, allowedTables);
        }

        String sql = variant.sql();
        List<Object> args = new ArrayList<>();
        for (NamedSqlQuery.Bind bind : variant.binds()) {
            String raw = requiredText(node, bind.name(), query.code());
            switch (bind.kind()) {
                case EQ -> args.add(raw);
                case LIKE -> args.add(toLike(raw));
                case IN -> {
                    List<String> items = splitIn(raw);
                    String token = "{in:" + bind.name() + "}";
                    if (!sql.contains(token)) {
                        throw new IllegalArgumentException("IN placeholder missing: " + token);
                    }
                    sql = sql.replace(token, placeholders(items.size()));
                    args.addAll(items);
                }
            }
        }
        if (sql.contains("{in:")) {
            throw new IllegalArgumentException("unbound IN placeholder in SQL");
        }
        sql = SqlGuard.applyLimit(sql, maxRows);
        return new BoundQuery(sql, args.toArray());
    }

    static NamedSqlQuery.Variant pickVariant(NamedSqlQuery query, String by) {
        if (!StringUtils.hasText(by)) {
            if (query.variants().containsKey("default")) {
                return query.variants().get("default");
            }
            throw new IllegalArgumentException(
                    "params.by is required for " + query.code() + ", allowed: " + query.byOptions());
        }
        String key = by.trim().toLowerCase(Locale.ROOT);
        NamedSqlQuery.Variant variant = query.variants().get(key);
        if (variant == null) {
            throw new IllegalArgumentException(
                    "unknown by=" + by + " for " + query.code() + ", allowed: " + query.byOptions());
        }
        return variant;
    }

    static String toLike(String raw) {
        String value = raw.trim();
        while (value.startsWith("%")) {
            value = value.substring(1);
        }
        while (value.endsWith("%")) {
            value = value.substring(0, value.length() - 1);
        }
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("LIKE value is empty");
        }
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

    static List<String> splitIn(String raw) {
        String[] parts = raw.split(",");
        List<String> items = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                items.add(part.trim());
            }
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("IN value is empty");
        }
        if (items.size() > MAX_IN_SIZE) {
            throw new IllegalArgumentException("IN list exceeds " + MAX_IN_SIZE + " values");
        }
        return items;
    }

    private static String placeholders(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }

    private static String requiredText(JsonNode node, String name, String code) {
        String value = text(node, name);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("missing params." + name + " for " + code);
        }
        return value.trim();
    }

    private static String text(JsonNode node, String name) {
        String value = JsonNodeUtils.asTextOr(node, null, name);
        return StringUtils.hasText(value) ? value : null;
    }
}
