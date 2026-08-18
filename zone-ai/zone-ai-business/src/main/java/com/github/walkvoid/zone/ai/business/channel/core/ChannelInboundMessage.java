package com.github.walkvoid.zone.ai.business.channel.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 通道无关的入站消息模型，屏蔽企微/飞书协议差异。
 */
public class ChannelInboundMessage {

    private final ChannelType channelType;
    private final String requestId;
    private final String messageId;
    private final String chatId;
    private final String chatType;
    private final String botId;
    private final String userId;
    private final String msgType;
    private final String textContent;
    private final List<ChannelImage> images;
    private final Map<String, Object> rawBody;

    private ChannelInboundMessage(Builder builder) {
        this.channelType = builder.channelType;
        this.requestId = builder.requestId;
        this.messageId = builder.messageId;
        this.chatId = builder.chatId;
        this.chatType = builder.chatType;
        this.botId = builder.botId;
        this.userId = builder.userId;
        this.msgType = builder.msgType;
        this.textContent = builder.textContent;
        this.images = builder.images == null || builder.images.isEmpty()
                ? List.of()
                : List.copyOf(builder.images);
        this.rawBody = builder.rawBody == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(builder.rawBody);
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatType() {
        return chatType;
    }

    public String getBotId() {
        return botId;
    }

    public String getUserId() {
        return userId;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getTextContent() {
        return textContent;
    }

    public List<ChannelImage> getImages() {
        return images;
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public Map<String, Object> getRawBody() {
        return rawBody;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ChannelType channelType;
        private String requestId;
        private String messageId;
        private String chatId;
        private String chatType;
        private String botId;
        private String userId;
        private String msgType;
        private String textContent;
        private List<ChannelImage> images;
        private Map<String, Object> rawBody;

        public Builder channelType(ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder chatId(String chatId) {
            this.chatId = chatId;
            return this;
        }

        public Builder chatType(String chatType) {
            this.chatType = chatType;
            return this;
        }

        public Builder botId(String botId) {
            this.botId = botId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder msgType(String msgType) {
            this.msgType = msgType;
            return this;
        }

        public Builder textContent(String textContent) {
            this.textContent = textContent;
            return this;
        }

        public Builder images(List<ChannelImage> images) {
            this.images = images;
            return this;
        }

        public Builder rawBody(Map<String, Object> rawBody) {
            this.rawBody = rawBody;
            return this;
        }

        public ChannelInboundMessage build() {
            return new ChannelInboundMessage(this);
        }
    }
}
