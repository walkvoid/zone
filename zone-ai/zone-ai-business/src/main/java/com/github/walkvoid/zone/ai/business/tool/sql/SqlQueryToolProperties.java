package com.github.walkvoid.zone.ai.business.tool.sql;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 供应链业务库只读查询配置。前缀 {@code zone.ai.tool.sql}。
 */
@ConfigurationProperties(prefix = "zone.ai.tool.sql")
public class SqlQueryToolProperties {

    /**
     * 总开关。false 时不建连，Tool 返回未启用。
     */
    private boolean enabled = false;

    private String url = "";
    private String username = "";
    private String password = "";

    /**
     * 允许查询的表（小写比对，建议 schema.table）。空则拒绝一切查表。
     */
    private List<String> allowedTables = new ArrayList<>(NamedSqlQueryCatalog.DEFAULT_ALLOWED_TABLES);

    /**
     * 结果里需要脱敏的列名片段（忽略大小写，列名包含即脱敏）。
     */
    private List<String> sensitiveColumns = new ArrayList<>(List.of(
            "password", "secret", "token", "api_key", "id_card", "mobile", "phone", "bank_card"));

    private int maxRows = 50;
    private int hardMaxRows = 200;
    private int queryTimeoutSeconds = 10;
    private int maxCellChars = 512;

    public Set<String> allowedTableSet() {
        return normalize(allowedTables);
    }

    public Set<String> sensitiveColumnSet() {
        return normalize(sensitiveColumns);
    }

    private static Set<String> normalize(List<String> raw) {
        if (raw == null) {
            return Set.of();
        }
        return raw.stream()
                .filter(StringUtils::hasText)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getAllowedTables() {
        return allowedTables;
    }

    public void setAllowedTables(List<String> allowedTables) {
        this.allowedTables = allowedTables;
    }

    public List<String> getSensitiveColumns() {
        return sensitiveColumns;
    }

    public void setSensitiveColumns(List<String> sensitiveColumns) {
        this.sensitiveColumns = sensitiveColumns;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getHardMaxRows() {
        return hardMaxRows;
    }

    public void setHardMaxRows(int hardMaxRows) {
        this.hardMaxRows = hardMaxRows;
    }

    public int getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public int getMaxCellChars() {
        return maxCellChars;
    }

    public void setMaxCellChars(int maxCellChars) {
        this.maxCellChars = maxCellChars;
    }
}
