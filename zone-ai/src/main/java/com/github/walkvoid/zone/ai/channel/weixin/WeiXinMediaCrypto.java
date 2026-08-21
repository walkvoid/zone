package com.github.walkvoid.zone.ai.channel.weixin;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 企微智能机器人多媒体解密：AES-256-CBC，IV 为密钥前 16 字节，PKCS#7（允许 1～32）。
 */
public final class WeiXinMediaCrypto {

    private WeiXinMediaCrypto() {
    }

    public static byte[] decrypt(byte[] encrypted, String aesKey) {
        if (encrypted == null || encrypted.length == 0) {
            throw new IllegalArgumentException("encrypted data is empty");
        }
        if (aesKey == null || aesKey.isBlank()) {
            return encrypted;
        }
        byte[] key = decodeKey(aesKey.trim());
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            key = Arrays.copyOf(key, 32);
        }
        byte[] iv = Arrays.copyOf(key, 16);
        byte[] aligned = align16(encrypted);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(aligned);
            return stripPkcs7(decrypted);
        } catch (Exception e) {
            throw new IllegalStateException("WeiXin media AES decrypt failed: " + e.getMessage(), e);
        }
    }

    static byte[] decodeKey(String aesKey) {
        if (aesKey.length() == 64 && aesKey.chars().allMatch(c ->
                (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
            return hexDecode(aesKey);
        }
        String padded = aesKey;
        int rem = padded.length() % 4;
        if (rem != 0) {
            padded = padded + "=".repeat(4 - rem);
        }
        try {
            return Base64.getDecoder().decode(padded.getBytes(StandardCharsets.US_ASCII));
        } catch (IllegalArgumentException e) {
            if (aesKey.length() % 2 == 0 && aesKey.chars().allMatch(c ->
                    (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return hexDecode(aesKey);
            }
            throw e;
        }
    }

    private static byte[] hexDecode(String hex) {
        int n = hex.length() / 2;
        byte[] out = new byte[n];
        for (int i = 0; i < n; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    private static byte[] align16(byte[] data) {
        int rem = data.length % 16;
        if (rem == 0) {
            return data;
        }
        return Arrays.copyOf(data, data.length + (16 - rem));
    }

    static byte[] stripPkcs7(byte[] decrypted) {
        if (decrypted.length == 0) {
            throw new IllegalArgumentException("decrypted data is empty");
        }
        int pad = decrypted[decrypted.length - 1] & 0xff;
        if (pad < 1 || pad > 32 || pad > decrypted.length) {
            throw new IllegalArgumentException("invalid PKCS#7 pad length: " + pad);
        }
        for (int i = decrypted.length - pad; i < decrypted.length; i++) {
            if ((decrypted[i] & 0xff) != pad) {
                throw new IllegalArgumentException("invalid PKCS#7 padding");
            }
        }
        return Arrays.copyOf(decrypted, decrypted.length - pad);
    }
}
