package com.github.walkvoid.zone.ai.business.web;

import org.springframework.util.StringUtils;

/**
 * 密钥展示：列表/详情不回发明文。
 */
public final class SecretDisplay {

    private SecretDisplay() {
    }

    public static String mask(String secret) {
        if (!StringUtils.hasText(secret)) {
            return "";
        }
        String trimmed = secret.trim();
        if (trimmed.length() <= 4) {
            return "••••";
        }
        return "••••" + trimmed.substring(trimmed.length() - 4);
    }
}
