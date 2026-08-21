package com.github.walkvoid.zone.ai.tool.log;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * BeeCloud 日志平台配置。前缀 {@code zone.ai.tool.beecloud}。
 * 账号也可通过环境变量 {@code BEELOG_USERNAME} / {@code BEELOG_PASSWORD} 覆盖。
 */
@ConfigurationProperties(prefix = "zone.ai.tool.beecloud")
public class BeeCloudProperties {

    private boolean enabled = true;
    private String baseUrl = "https://beecloud.llschain.com";
    /**
     * SSO 根地址。留空则从 OIDC 跳转自动解析。
     */
    private String authBaseUrl = "";
    private String username = "";
    private String password = "";
    private String tenantId = "8b434d97-87ab-49f9-a82d-8d3c82df6d5e";
    private String project = "jinkoscf";
    private String tokenFile = System.getProperty("user.home") + "/.zone-ai/beecloud-token";
    private int requestTimeoutSeconds = 20;

    public String baseUrl() {
        return trimSlash(baseUrl);
    }

    public String authBaseUrl() {
        return trimSlash(authBaseUrl);
    }

    public String username() {
        return firstNonBlank(username, System.getenv("BEELOG_USERNAME"));
    }

    public String password() {
        return firstNonBlank(password, System.getenv("BEELOG_PASSWORD"));
    }

    public Path tokenFilePath() {
        String path = StringUtils.hasText(tokenFile)
                ? tokenFile.trim()
                : System.getProperty("user.home") + "/.zone-ai/beecloud-token";
        return Paths.get(path).toAbsolutePath();
    }

    public String searchUrl() {
        return baseUrl() + "/beelog/api/v1/tenants/" + tenantId.trim()
                + "/applications/search?project=" + project.trim();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public void setAuthBaseUrl(String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTokenFile() {
        return tokenFile;
    }

    public void setTokenFile(String tokenFile) {
        this.tokenFile = tokenFile;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public int requestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    private static String firstNonBlank(String configured, String env) {
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        return env == null ? "" : env.trim();
    }

    private static String trimSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
