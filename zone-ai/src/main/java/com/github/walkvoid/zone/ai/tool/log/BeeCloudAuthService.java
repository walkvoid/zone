package com.github.walkvoid.zone.ai.tool.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.walkvoid.wvframework.utils.JsonNodeUtils;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BeeCloudAuthService {

    private static final Logger log = LoggerFactory.getLogger(BeeCloudAuthService.class);
    private static final Pattern TOKEN_COOKIE_PATTERN = Pattern.compile("(?i)(?:^|;\\s*)token=([^;\\s]+)");

    private final BeeCloudProperties properties;
    private final HttpClient httpClient;
    private volatile String discoveredAuthBaseUrl = "";

    public BeeCloudAuthService(BeeCloudProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .followRedirects(HttpClient.Redirect.NEVER)
                .cookieHandler(new java.net.CookieManager())
                .build();
    }

    public String login() {
        validateCredentials();

        try {
            String requestId = startOidcAndGetRequestId();
            String publicKeyPem = fetchPublicKey();
            String encryptedPassword = encryptPassword(publicKeyPem);
            postLogin(requestId, encryptedPassword);
            String redirectUrl = fetchCallbackRedirect(requestId);
            return exchangeToken(redirectUrl);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("BeeCloud login interrupted", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("BeeCloud login failed: " + ex.getMessage(), ex);
        }
    }

    private void validateCredentials() {
        if (!StringUtils.hasText(properties.username())) {
            throw new IllegalStateException(
                    "BeeCloud username is empty. Set zone.ai.tool.beecloud.username or BEELOG_USERNAME.");
        }
        if (!StringUtils.hasText(properties.password())) {
            throw new IllegalStateException(
                    "BeeCloud password is empty. Set zone.ai.tool.beecloud.password or BEELOG_PASSWORD.");
        }
    }

    private String startOidcAndGetRequestId() throws IOException, InterruptedException {
        String currentUrl = properties.baseUrl() + "/u5/api/v1/oidc/login";
        for (int hop = 0; hop < 10; hop++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(currentUrl))
                    .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                    .header("Accept", "text/html,application/json")
                    .header("User-Agent", defaultUserAgent())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String location = response.headers().firstValue("Location").orElse("");
                if (!StringUtils.hasText(location)) {
                    break;
                }
                currentUrl = resolveRedirectUrl(currentUrl, location);
                String requestId = extractQueryParam(currentUrl, "req");
                if (StringUtils.hasText(requestId)) {
                    rememberAuthBase(currentUrl);
                    log.info("SSO request_id registered: {}", requestId);
                    return requestId;
                }
                continue;
            }
            String requestId = extractQueryParam(currentUrl, "req");
            if (StringUtils.hasText(requestId)) {
                rememberAuthBase(currentUrl);
                log.info("SSO request_id from login page: {}", requestId);
                return requestId;
            }
            throw new IllegalStateException(
                    "OIDC start failed, status=" + status + ", url=" + currentUrl + ", body=" + response.body());
        }
        throw new IllegalStateException("OIDC start did not return request_id (req)");
    }

    private String fetchPublicKey() throws IOException, InterruptedException {
        String url = authBaseUrl()
                + "/sso/api/v1/captcha?captcha_type=math&login_account="
                + URLEncoder.encode(properties.username(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .header("Accept", "application/json")
                .header("User-Agent", defaultUserAgent())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = parseJson(response.body(), "fetch public key");

        if (JsonNodeUtils.asInt(body, -1, "code") != 0) {
            throw new IllegalStateException("Failed to fetch public key: " + response.body());
        }

        String publicKey = JsonNodeUtils.asText(body, "data", "key");
        if (!StringUtils.hasText(publicKey)) {
            throw new IllegalStateException("Public key missing in captcha response");
        }
        log.info("Fetched SSO public key for user {}", properties.username());
        return publicKey;
    }

    private String encryptPassword(String publicKeyPem) {
        try {
            return LlsLoginEncryptor.encryptPassword(properties.password(), publicKeyPem);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encrypt password", ex);
        }
    }

    private void postLogin(String requestId, String encryptedPassword) throws IOException, InterruptedException {
        Map<String, String> loginBody = new LinkedHashMap<>();
        loginBody.put("username", properties.username());
        loginBody.put("password", encryptedPassword);
        loginBody.put("request_id", requestId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authBaseUrl() + "/sso/api/v1/login"))
                .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Accept", "application/json")
                .header("Origin", authBaseUrl())
                .header("Referer", authBaseUrl() + "/sso/login")
                .header("User-Agent", defaultUserAgent())
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getObjectMapper().writeValueAsString(loginBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = parseJson(response.body(), "login");

        if (JsonNodeUtils.asInt(body, -1, "code") != 0) {
            String message = JsonNodeUtils.firstText(body, "unknown error", "message", "msg");
            throw new IllegalStateException("Login failed: " + message);
        }
        log.info("BeeCloud SSO login succeeded for user {}", properties.username());
    }

    private String fetchCallbackRedirect(String requestId) throws IOException, InterruptedException {
        String url = authBaseUrl() + "/sso/api/v1/callback?request_id="
                + URLEncoder.encode(requestId, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .header("Accept", "text/html,application/json")
                .header("User-Agent", defaultUserAgent())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status < 300 || status >= 400) {
            throw new IllegalStateException("Callback did not redirect, status=" + status + ", body=" + response.body());
        }

        String location = response.headers().firstValue("Location").orElse("");
        if (!StringUtils.hasText(location)) {
            throw new IllegalStateException("Callback redirect Location header is empty");
        }
        log.info("SSO callback redirected, status={}", status);
        return location;
    }

    private String exchangeToken(String redirectUrl) throws IOException, InterruptedException {
        java.net.CookieManager cookieManager = new java.net.CookieManager();
        HttpClient redirectClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                .followRedirects(HttpClient.Redirect.NEVER)
                .cookieHandler(cookieManager)
                .build();

        String currentUrl = redirectUrl;
        for (int hop = 0; hop < 10; hop++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(currentUrl))
                    .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                    .header("Accept", "text/html,application/json")
                    .header("User-Agent", defaultUserAgent())
                    .GET()
                    .build();

            HttpResponse<String> response = redirectClient.send(request, HttpResponse.BodyHandlers.ofString());
            String token = extractTokenFromHeaders(response.headers().map());
            if (StringUtils.hasText(token)) {
                log.info("BeeCloud token cookie received after {} hop(s), length={}", hop + 1, token.length());
                return token;
            }

            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String location = response.headers().firstValue("Location").orElse("");
                if (!StringUtils.hasText(location)) {
                    break;
                }
                currentUrl = resolveRedirectUrl(currentUrl, location);
                continue;
            }
            break;
        }

        throw new IllegalStateException("Token cookie not found after OIDC callback");
    }

    private String authBaseUrl() {
        if (StringUtils.hasText(properties.authBaseUrl())) {
            return properties.authBaseUrl();
        }
        if (StringUtils.hasText(discoveredAuthBaseUrl)) {
            return discoveredAuthBaseUrl;
        }
        throw new IllegalStateException(
                "BeeCloud auth-base-url is empty and SSO host was not discovered from OIDC. "
                        + "Set zone.ai.tool.beecloud.auth-base-url.");
    }

    private void rememberAuthBase(String url) {
        if (StringUtils.hasText(discoveredAuthBaseUrl) || !StringUtils.hasText(url)) {
            return;
        }
        URI uri = URI.create(url);
        if (uri.getScheme() == null || uri.getAuthority() == null) {
            return;
        }
        discoveredAuthBaseUrl = uri.getScheme() + "://" + uri.getAuthority();
        log.info("Discovered BeeCloud SSO host: {}", discoveredAuthBaseUrl);
    }

    private String resolveRedirectUrl(String currentUrl, String location) {
        URI base = URI.create(currentUrl);
        URI resolved = base.resolve(location);
        return resolved.toString();
    }

    static String extractQueryParam(String url, String name) {
        URI uri = URI.create(url);
        String query = uri.getQuery();
        if (!StringUtils.hasText(query)) {
            return "";
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            if (name.equals(key)) {
                return idx >= 0 ? pair.substring(idx + 1) : "";
            }
        }
        return "";
    }

    private String extractTokenFromHeaders(Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (!"set-cookie".equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            for (String cookieHeader : entry.getValue()) {
                Matcher matcher = TOKEN_COOKIE_PATTERN.matcher(cookieHeader);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return "";
    }

    private JsonNode parseJson(String body, String step) throws IOException {
        if (!StringUtils.hasText(body)) {
            throw new IllegalStateException("Empty response while trying to " + step);
        }
        return JsonUtils.getObjectMapper().readTree(body);
    }

    private String defaultUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    }
}
