package com.github.walkvoid.zone.ai.tool.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class TokenStore {

    private static final Logger log = LoggerFactory.getLogger(TokenStore.class);

    private final BeeCloudProperties properties;
    private final BeeCloudAuthService authService;

    public TokenStore(BeeCloudProperties properties, BeeCloudAuthService authService) {
        this.properties = properties;
        this.authService = authService;
    }

    public synchronized String resolveToken(String overrideToken) {
        if (StringUtils.hasText(overrideToken)) {
            return overrideToken.trim();
        }
        String fileToken = readTokenFile();
        if (StringUtils.hasText(fileToken)) {
            return fileToken;
        }
        return loginAndPersist();
    }

    public synchronized String refreshToken() {
        return loginAndPersist();
    }

    private String readTokenFile() {
        Path tokenFile = properties.tokenFilePath();
        if (!Files.isRegularFile(tokenFile)) {
            return "";
        }
        try {
            return Files.readString(tokenFile, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            log.warn("Failed to read token file {}: {}", tokenFile, ex.getMessage());
            return "";
        }
    }

    private String loginAndPersist() {
        String token = authService.login();
        persistToken(token);
        return token;
    }

    private void persistToken(String token) {
        Path tokenFile = properties.tokenFilePath();
        try {
            Path parent = tokenFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(tokenFile, token, StandardCharsets.UTF_8);
            log.info("BeeCloud token saved to {}", tokenFile);
        } catch (IOException ex) {
            log.warn("Failed to write token file {}: {}", tokenFile, ex.getMessage());
        }
    }
}
