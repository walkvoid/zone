package com.github.walkvoid.zone.ai.tool.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 供应链业务库只读连接。账号复用 {@code zone.ai.tool.sql}，
 * 与 MyBatis 主库 {@code zone-ai} 分池：本连接只读，且 SQL 使用 jinkoscf_* 库表全名。
 */
@Component
public class SqlQuerySupport {

    private static final Logger log = LoggerFactory.getLogger(SqlQuerySupport.class);

    private final SqlQueryToolProperties properties;
    private volatile HikariDataSource dataSource;
    private volatile JdbcTemplate jdbcTemplate;

    public SqlQuerySupport(SqlQueryToolProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void start() {
        if (!properties.isEnabled()) {
            log.info("SqlQueryTool disabled (zone.ai.tool.sql.enabled=false)");
            return;
        }
        log.info("SqlQueryTool connecting, url={}, username={}", properties.getUrl(), properties.getUsername());
        if (!StringUtils.hasText(properties.getUrl())) {
            throw new IllegalStateException("zone.ai.tool.sql.enabled=true but url is empty");
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(properties.getUrl().trim());
        cfg.setUsername(properties.getUsername());
        cfg.setPassword(properties.getPassword());
        cfg.setReadOnly(true);
        cfg.setMaximumPoolSize(2);
        cfg.setMinimumIdle(0);
        cfg.setConnectionTimeout(10_000);
        cfg.setIdleTimeout(30_000);
        cfg.setMaxLifetime(600_000);
        cfg.setInitializationFailTimeout(10_000);
        cfg.setPoolName("zone-ai-sql-query");
        this.dataSource = new HikariDataSource(cfg);
        JdbcTemplate template = new JdbcTemplate(this.dataSource);
        template.setQueryTimeout(Math.max(1, properties.getQueryTimeoutSeconds()));
        this.jdbcTemplate = template;
        log.info("SqlQueryTool connected, allowedTables={}", properties.allowedTableSet());
    }

    @PreDestroy
    public void stop() {
        HikariDataSource ds = this.dataSource;
        this.dataSource = null;
        this.jdbcTemplate = null;
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    public boolean isReady() {
        return properties.isEnabled() && jdbcTemplate != null;
    }

    public JdbcTemplate jdbc() {
        JdbcTemplate template = this.jdbcTemplate;
        if (template == null) {
            throw new IllegalStateException("SQL query tool is not connected");
        }
        return template;
    }

    public SqlQueryToolProperties properties() {
        return properties;
    }
}
