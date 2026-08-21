package com.github.walkvoid.zone.ai.channel.weixin;

import com.github.walkvoid.zone.ai.channel.core.ChannelImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载企微图片并按 aeskey 解密。
 */
@Component
public class WeiXinMediaDownloader {

    private static final Logger log = LoggerFactory.getLogger(WeiXinMediaDownloader.class);
    static final int DEFAULT_MAX_BYTES = 5 * 1024 * 1024;
    static final int MAX_IMAGES = 4;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public List<DownloadedImage> download(List<ChannelImage> images) {
        return download(images, DEFAULT_MAX_BYTES);
    }

    public List<DownloadedImage> download(List<ChannelImage> images, int maxBytes) {
        List<DownloadedImage> result = new ArrayList<>();
        if (images == null || images.isEmpty()) {
            return result;
        }
        int limit = Math.min(images.size(), MAX_IMAGES);
        for (int i = 0; i < limit; i++) {
            ChannelImage image = images.get(i);
            if (image == null || !image.hasUrl()) {
                continue;
            }
            result.add(downloadOne(image, maxBytes));
        }
        return result;
    }

    DownloadedImage downloadOne(ChannelImage image, int maxBytes) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(image.url()))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("HTTP " + response.statusCode());
            }
            byte[] raw = response.body() == null ? new byte[0] : response.body();
            if (raw.length > maxBytes) {
                throw new IllegalStateException("image too large: " + raw.length + " bytes");
            }
            byte[] bytes = raw;
            if (StringUtils.hasText(image.aesKey())) {
                try {
                    byte[] decrypted = WeiXinMediaCrypto.decrypt(raw, image.aesKey());
                    if (looksLikeImage(decrypted) || !looksLikeImage(raw)) {
                        bytes = decrypted;
                    }
                } catch (Exception e) {
                    log.warn("WeiXin image decrypt failed, try raw bytes: {}", e.getMessage());
                    if (!looksLikeImage(raw)) {
                        throw e;
                    }
                }
            }
            if (bytes.length > maxBytes) {
                throw new IllegalStateException("decrypted image too large: " + bytes.length + " bytes");
            }
            MimeType mime = detectMime(bytes);
            log.info("WeiXin image downloaded, bytes={}, mime={}", bytes.length, mime);
            return new DownloadedImage(bytes, mime);
        } catch (Exception e) {
            throw new IllegalStateException("download image failed: " + e.getMessage(), e);
        }
    }

    static boolean looksLikeImage(byte[] bytes) {
        return sniffMime(bytes) != null;
    }

    static MimeType detectMime(byte[] bytes) {
        MimeType sniffed = sniffMime(bytes);
        return sniffed != null ? sniffed : MimeTypeUtils.IMAGE_JPEG;
    }

    private static MimeType sniffMime(byte[] bytes) {
        if (bytes == null || bytes.length < 3) {
            return null;
        }
        if ((bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return MimeTypeUtils.IMAGE_JPEG;
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4e
                && bytes[3] == 0x47) {
            return MimeTypeUtils.IMAGE_PNG;
        }
        if (bytes.length >= 6
                && bytes[0] == 'G'
                && bytes[1] == 'I'
                && bytes[2] == 'F') {
            return MimeTypeUtils.parseMimeType("image/gif");
        }
        if (bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P') {
            return MimeTypeUtils.parseMimeType("image/webp");
        }
        return null;
    }

    public record DownloadedImage(byte[] bytes, MimeType mimeType) {
    }
}
