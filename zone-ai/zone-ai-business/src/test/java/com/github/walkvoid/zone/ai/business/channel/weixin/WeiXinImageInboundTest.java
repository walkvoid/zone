package com.github.walkvoid.zone.ai.business.channel.weixin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelImage;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeiXinImageInboundTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void decryptsAes256CbcWithHexKey() throws Exception {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) i;
        }
        String hex = toHex(key);
        byte[] plain = "hello-wecom-image".getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(Arrays.copyOf(key, 16)));
        byte[] encrypted = cipher.doFinal(plain);
        assertArrayEquals(plain, WeiXinMediaCrypto.decrypt(encrypted, hex));
    }

    @Test
    void extractImagesFromImageMessage() throws Exception {
        JsonNode body = mapper.readTree("""
                {
                  "msgtype": "image",
                  "image": {
                    "url": "https://example.com/a.jpg",
                    "aeskey": "abc"
                  }
                }
                """);
        List<ChannelImage> images = WeiXinMessageBridge.extractImages(body);
        assertEquals(1, images.size());
        assertEquals("https://example.com/a.jpg", images.get(0).url());
        assertEquals("abc", images.get(0).aesKey());
    }

    @Test
    void extractImagesFromMixedMessage() throws Exception {
        JsonNode body = mapper.readTree("""
                {
                  "msgtype": "mixed",
                  "mixed": {
                    "msg_item": [
                      { "msgtype": "text", "text": { "content": "@bot 看图" } },
                      { "msgtype": "image", "image": { "url": "https://example.com/b.png", "aeskey": "k2" } }
                    ]
                  }
                }
                """);
        List<ChannelImage> images = WeiXinMessageBridge.extractImages(body);
        assertEquals(1, images.size());
        assertEquals("https://example.com/b.png", images.get(0).url());
        assertTrue(WeiXinMediaDownloader.looksLikeImage(new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00}));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
