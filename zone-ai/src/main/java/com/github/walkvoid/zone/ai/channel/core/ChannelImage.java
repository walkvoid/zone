package com.github.walkvoid.zone.ai.channel.core;

/**
 * 入站图片：企微长连接给出临时下载地址和 AES 密钥。
 */
public record ChannelImage(String url, String aesKey) {

    public boolean hasUrl() {
        return url != null && !url.isBlank();
    }
}
