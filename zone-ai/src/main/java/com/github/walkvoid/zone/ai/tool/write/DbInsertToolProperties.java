package com.github.walkvoid.zone.ai.tool.write;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据库插入工具配置。前缀 {@code zone.ai.tool.db-insert}。
 * 使用 zone-ai 主库 DataSource（可写），与只读 SqlQueryTool 业务库分离。
 */
@ConfigurationProperties(prefix = "zone.ai.tool.db-insert")
public class DbInsertToolProperties {

    private boolean enabled = true;

    /** 允许 INSERT 的表（小写比对）。默认仅归档表。 */
    private List<String> allowedTables = new ArrayList<>(List.of("prompt_result_archive"));

    private int maxContentChars = 500_000;
    private int maxColumns = 40;

    public Set<String> allowedTableSet() {
        if (allowedTables == null) {
            return Set.of();
        }
        return allowedTables.stream()
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

    public List<String> getAllowedTables() {
        return allowedTables;
    }

    public void setAllowedTables(List<String> allowedTables) {
        this.allowedTables = allowedTables;
    }

    public int getMaxContentChars() {
        return maxContentChars;
    }

    public void setMaxContentChars(int maxContentChars) {
        this.maxContentChars = maxContentChars;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }
}
