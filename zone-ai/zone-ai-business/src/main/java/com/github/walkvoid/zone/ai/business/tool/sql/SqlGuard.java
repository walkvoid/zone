package com.github.walkvoid.zone.ai.business.tool.sql;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 只读 SQL 校验：单条 SELECT、表白名单、强制 LIMIT。
 */
public final class SqlGuard {

    private static final Pattern IDENT = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private static final Pattern FORBIDDEN = Pattern.compile(
            "(?i)(?<!\\w)("
                    + "INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE|REPLACE|MERGE|"
                    + "GRANT|REVOKE|LOAD|CALL|HANDLER|LOCK|UNLOCK|PREPARE|EXECUTE|DEALLOCATE|"
                    + "OUTFILE|DUMPFILE|BENCHMARK|SLEEP|GET_LOCK|RELEASE_LOCK|"
                    + "INTO|PROCEDURE|FUNCTION|TRIGGER|EVENT|VIEW|"
                    + "FOR\\s+UPDATE|FOR\\s+SHARE|LOCK\\s+IN\\s+SHARE\\s+MODE"
                    + ")(?!\\w)");

    private static final Pattern FROM_JOIN_TABLE = Pattern.compile(
            "(?i)\\b(?:FROM|JOIN)\\s+(?:(\\w+)\\s*\\.\\s*)?`?([A-Za-z_][A-Za-z0-9_]*)`?");

    private static final Pattern COMMA_TABLE = Pattern.compile(
            "(?i),\\s*(?:(\\w+)\\s*\\.\\s*)?`?([A-Za-z_][A-Za-z0-9_]*)`?");

    private static final Pattern LIMIT_TAIL = Pattern.compile(
            "(?i)\\sLIMIT\\s+(\\d+)\\s*(?:,\\s*(\\d+))?(?:\\s+OFFSET\\s+(\\d+))?\\s*;?\\s*$");

    private static final Pattern CONSTANT_SELECT = Pattern.compile("(?i)^SELECT\\s+\\d+\\s*$");

    private static final Set<String> BLOCKED_SCHEMA = Set.of(
            "mysql", "sys", "performance_schema", "information_schema");

    private SqlGuard() {
    }

    public static String requireIdent(String name, String kind) {
        if (!StringUtils.hasText(name) || !IDENT.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException(kind + " is not a valid identifier: " + name);
        }
        return name.trim();
    }

    /**
     * 校验表白名单，返回规范名（优先 schema.table）。
     */
    public static String requireAllowedTable(String table, Set<String> allowedTables) {
        if (!StringUtils.hasText(table)) {
            throw new IllegalArgumentException("table is empty");
        }
        String raw = table.trim().replace("`", "").toLowerCase(Locale.ROOT);
        String schema = null;
        String tableName = raw;
        int dot = raw.indexOf('.');
        if (dot > 0) {
            schema = requireIdent(raw.substring(0, dot), "schema").toLowerCase(Locale.ROOT);
            tableName = requireIdent(raw.substring(dot + 1), "table").toLowerCase(Locale.ROOT);
        } else {
            tableName = requireIdent(raw, "table").toLowerCase(Locale.ROOT);
        }
        if (schema != null && BLOCKED_SCHEMA.contains(schema)) {
            throw new IllegalArgumentException("system schema is not allowed: " + schema);
        }
        String qualified = schema == null ? tableName : schema + "." + tableName;
        String canonical = findAllowed(qualified, tableName, schema != null, allowedTables);
        if (canonical == null) {
            throw new IllegalArgumentException("table is not in allow-list: " + table);
        }
        return canonical;
    }

    public static String quoteQualified(String table) {
        String canonical = table.trim().replace("`", "");
        int dot = canonical.indexOf('.');
        if (dot > 0) {
            return "`" + canonical.substring(0, dot) + "`.`" + canonical.substring(dot + 1) + "`";
        }
        return "`" + canonical + "`";
    }

    /**
     * 给已校验 SQL 补/截断 LIMIT。
     */
    public static String applyLimit(String sql, int maxRows) {
        int limit = Math.max(1, maxRows);
        Matcher lim = LIMIT_TAIL.matcher(sql);
        if (!lim.find()) {
            return sql + " LIMIT " + limit;
        }
        if (lim.group(2) != null) {
            int offset = Integer.parseInt(lim.group(1));
            int count = Math.min(Integer.parseInt(lim.group(2)), limit);
            return sql.substring(0, lim.start()) + " LIMIT " + offset + ", " + count;
        }
        int count = Math.min(Integer.parseInt(lim.group(1)), limit);
        String offsetPart = lim.group(3) != null ? " OFFSET " + lim.group(3) : "";
        return sql.substring(0, lim.start()) + " LIMIT " + count + offsetPart;
    }

    private static String findAllowed(String qualified, String tableName, boolean qualifiedInput,
                                      Set<String> allowedTables) {
        if (allowedTables == null || allowedTables.isEmpty()) {
            return null;
        }
        if (allowedTables.contains(qualified)) {
            return qualified;
        }
        List<String> matches = new ArrayList<>();
        for (String allowed : allowedTables) {
            if (allowed.equals(qualified) || allowed.equals(tableName)) {
                matches.add(allowed);
            } else if (!qualifiedInput && allowed.endsWith("." + tableName)) {
                matches.add(allowed);
            }
        }
        LinkedHashSet<String> uniq = new LinkedHashSet<>(matches);
        if (uniq.size() == 1) {
            return uniq.iterator().next();
        }
        if (uniq.size() > 1) {
            throw new IllegalArgumentException(
                    "table name is ambiguous, use schema.table: " + tableName + " -> " + uniq);
        }
        return null;
    }

    /**
     * 校验并返回可执行的单条 SELECT（已补/截断 LIMIT）。
     */
    public static String guardSelect(String rawSql, Set<String> allowedTables, int maxRows) {
        if (!StringUtils.hasText(rawSql)) {
            throw new IllegalArgumentException("sql is empty");
        }
        String sql = rawSql.trim();
        if (sql.indexOf('\u0000') >= 0) {
            throw new IllegalArgumentException("sql contains illegal characters");
        }
        if (sql.contains("--") || sql.contains("/*") || sql.contains("*/") || sql.contains("#")) {
            throw new IllegalArgumentException("SQL comments are not allowed");
        }
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        if (sql.contains(";")) {
            throw new IllegalArgumentException("multiple SQL statements are not allowed");
        }
        if (sql.contains("@")) {
            throw new IllegalArgumentException("user variables are not allowed");
        }

        if (!isSelectOrWith(sql)) {
            throw new IllegalArgumentException("only a single SELECT (or WITH ... SELECT) is allowed");
        }
        if (FORBIDDEN.matcher(sql).find()) {
            throw new IllegalArgumentException("SQL contains a forbidden keyword");
        }

        Set<String> tables = extractTables(sql);
        if (tables.isEmpty()) {
            if (!CONSTANT_SELECT.matcher(sql).matches()) {
                throw new IllegalArgumentException("query must target a whitelisted table");
            }
        } else {
            for (String table : tables) {
                requireAllowedTable(table, allowedTables);
            }
        }

        int limit = Math.max(1, maxRows);
        return applyLimit(sql, limit);
    }

    private static boolean isSelectOrWith(String sql) {
        if (sql.regionMatches(true, 0, "SELECT", 0, 6)) {
            return sql.length() == 6 || isSelectSep(sql.charAt(6));
        }
        return sql.regionMatches(true, 0, "WITH", 0, 4)
                && sql.length() > 4
                && Character.isWhitespace(sql.charAt(4));
    }

    private static boolean isSelectSep(char c) {
        return Character.isWhitespace(c) || c == '/' || c == '(' || c == '*';
    }

    static Set<String> extractTables(String sql) {
        Set<String> tables = new LinkedHashSet<>();
        Matcher fromJoin = FROM_JOIN_TABLE.matcher(sql);
        while (fromJoin.find()) {
            int tableStart = fromJoin.start(2);
            if (isSubquery(sql, tableStart)) {
                continue;
            }
            addTable(tables, fromJoin.group(1), fromJoin.group(2));
        }
        Matcher fromClause = Pattern.compile(
                "(?i)\\bFROM\\s+(.+?)(?=\\s+WHERE\\b|\\s+GROUP\\s+BY\\b|\\s+HAVING\\b|\\s+ORDER\\s+BY\\b|\\s+LIMIT\\b|\\s+UNION\\b|\\s+WINDOW\\b|\\s+FOR\\s+|$)")
                .matcher(sql);
        while (fromClause.find()) {
            Matcher comma = COMMA_TABLE.matcher(fromClause.group(1));
            while (comma.find()) {
                if (isSubquery(fromClause.group(1), comma.start(2))) {
                    continue;
                }
                addTable(tables, comma.group(1), comma.group(2));
            }
        }
        return tables;
    }

    private static boolean isSubquery(String sql, int tableStart) {
        int i = tableStart - 1;
        while (i >= 0 && Character.isWhitespace(sql.charAt(i))) {
            i--;
        }
        return i >= 0 && sql.charAt(i) == '(';
    }

    private static void addTable(Set<String> tables, String schema, String table) {
        if (schema != null && BLOCKED_SCHEMA.contains(schema.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("system schema is not allowed: " + schema);
        }
        if (!StringUtils.hasText(table)) {
            return;
        }
        if (StringUtils.hasText(schema)) {
            tables.add(schema + "." + table);
        } else {
            tables.add(table);
        }
    }
}
