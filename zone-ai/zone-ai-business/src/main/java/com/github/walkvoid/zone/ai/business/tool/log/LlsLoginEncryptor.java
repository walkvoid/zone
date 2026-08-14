package com.github.walkvoid.zone.ai.business.tool.log;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Aligns with frontend JSEncrypt in {@code handleSubmit}:
 * <pre>
 *   const payload = { password, timestamp: Date.now().toString() };
 *   encryptor.setPublicKey(publicKeyPem);
 *   encryptor.encrypt(JSON.stringify(payload));
 * </pre>
 */
public final class LlsLoginEncryptor {

    private LlsLoginEncryptor() {
    }

    public static String encryptPassword(String plainPassword, String publicKeyPem) throws Exception {
        return encryptPassword(plainPassword, publicKeyPem, String.valueOf(System.currentTimeMillis()));
    }

    public static String encryptPassword(String plainPassword, String publicKeyPem, String timestamp) throws Exception {
        String payload = buildPayload(plainPassword, timestamp);
        PublicKey publicKey = parsePublicKey(publicKeyPem);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Matches {@code JSON.stringify({ password, timestamp })}: no spaces, password first.
     */
    public static String buildPayload(String plainPassword, String timestamp) {
        return "{\"password\":\"" + escapeJson(plainPassword)
                + "\",\"timestamp\":\"" + escapeJson(timestamp) + "\"}";
    }

    public static PublicKey parsePublicKey(String publicKeyPem) throws Exception {
        String pem = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
