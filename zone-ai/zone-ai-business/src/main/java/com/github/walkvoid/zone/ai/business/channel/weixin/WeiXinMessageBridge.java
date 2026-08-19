package com.github.walkvoid.zone.ai.business.channel.weixin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.utils.JsonNodeUtils;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelImage;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelReplySink;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 企业微信帧解析与业务桥接：协议 JSON ↔ {@link ChannelInboundMessage}/{@link ChannelReplySink}。
 */
public class WeiXinMessageBridge {

    private static final Logger log = LoggerFactory.getLogger(WeiXinMessageBridge.class);

    private final ChannelMessageHandler messageHandler;
    private final String welcomeText;
    private final Consumer<String> frameSender;
    private final String configuredBotId;

    public WeiXinMessageBridge(ChannelMessageHandler messageHandler,
                               String welcomeText,
                               Consumer<String> frameSender,
                               String configuredBotId) {
        this.messageHandler = messageHandler;
        this.welcomeText = welcomeText;
        this.frameSender = frameSender;
        this.configuredBotId = configuredBotId;
    }

    public void onFrame(String text) {
        try {
            JsonNode root = JsonUtils.getObjectMapper().readTree(text);
            String cmd = JsonNodeUtils.asTextOr(root, null, "cmd");
            if (cmd == null || cmd.isBlank()) {
                // 订阅/心跳响应通常只有 headers + errcode
                int errcode = JsonNodeUtils.asInt(root, Integer.MIN_VALUE, "errcode");
                if (errcode != Integer.MIN_VALUE) {
                    log.info("WeiXin response without cmd, errcode={}, errmsg={}",
                            errcode, JsonNodeUtils.asText(root, "errmsg"));
                } else {
                    log.info("WeiXin frame has no cmd, keys={}", fieldNames(root));
                }
                return;
            }

            log.info("WeiXin onFrame cmd={}", cmd);
            switch (cmd) {
                case WeiXinCmd.MSG_CALLBACK -> handleMsgCallback(root);
                case WeiXinCmd.EVENT_CALLBACK -> handleEventCallback(root);
                case WeiXinCmd.PING -> log.info("WeiXin ping echo ignored");
                default -> log.info("WeiXin unhandled cmd={}, bodyKeys={}", cmd, fieldNames(JsonNodeUtils.path(root, "body")));
            }
        } catch (Exception e) {
            log.error("WeiXin frame handle failed: {}", e.getMessage(), e);
        }
    }

    private void handleMsgCallback(JsonNode root) {
        String reqId = JsonNodeUtils.asText(root, "headers", "req_id");
        JsonNode body = JsonNodeUtils.path(root, "body");
        log.info("WeiXin msg_callback reqId={}, aibotid={}, chatid={}, chattype={}, userid={}, msgtype={}",
                reqId,
                resolveBotId(body),
                JsonNodeUtils.asText(body, "chatid"),
                JsonNodeUtils.asText(body, "chattype"),
                JsonNodeUtils.asText(body, "from", "userid"),
                JsonNodeUtils.asText(body, "msgtype"));
        ChannelInboundMessage message = ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .requestId(reqId)
                .messageId(JsonNodeUtils.asTextOr(body, null, "msgid"))
                .chatId(JsonNodeUtils.asTextOr(body, null, "chatid"))
                .chatType(JsonNodeUtils.asTextOr(body, null, "chattype"))
                .botId(resolveBotId(body))
                .userId(JsonNodeUtils.asTextOr(body, null, "from", "userid"))
                .msgType(JsonNodeUtils.asTextOr(body, null, "msgtype"))
                .textContent(extractText(body))
                .images(extractImages(body))
                .rawBody(toMap(body))
                .build();

        ChannelReplySink sink = new WeiXinReplySink(reqId);
        try {
            log.info("WeiXin dispatch onMessage, handler={}, botId={}, msgType={}, textPreview={}, images={}",
                    messageHandler.getClass().getSimpleName(),
                    message.getBotId(),
                    message.getMsgType(),
                    preview(message.getTextContent()),
                    message.getImages().size());
            messageHandler.onMessage(message, sink);
        } catch (Exception e) {
            log.error("WeiXin message handler error", e);
            sink.replyText("处理消息时出错，请稍后重试。");
        }
    }

    private String resolveBotId(JsonNode body) {
        String inbound = JsonNodeUtils.firstText(body, "", "aibotid", "botid");
        if (inbound != null && !inbound.isBlank()) {
            return inbound.trim();
        }
        return configuredBotId;
    }

    private static String preview(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.replace("\n", " ");
        return trimmed.length() > 120 ? trimmed.substring(0, 120) + "..." : trimmed;
    }

    private static String fieldNames(JsonNode node) {
        if (node == null || !node.isObject()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        node.fieldNames().forEachRemaining(name -> {
            if (!sb.isEmpty()) {
                sb.append(',');
            }
            sb.append(name);
        });
        return sb.toString();
    }

    private void handleEventCallback(JsonNode root) {
        String reqId = JsonNodeUtils.asText(root, "headers", "req_id");
        JsonNode body = JsonNodeUtils.path(root, "body");
        String eventType = JsonNodeUtils.firstText(body, "unknown", "eventtype", "event_type");
        ChannelReplySink sink = new WeiXinReplySink(reqId);
        try {
            messageHandler.onEvent(ChannelType.WEIXIN, eventType, reqId, sink);
            if ("enter_chat".equalsIgnoreCase(eventType) && welcomeText != null && !welcomeText.isBlank()) {
                sink.replyWelcome(welcomeText);
            }
        } catch (Exception e) {
            log.error("WeiXin event handler error, eventType={}", eventType, e);
        }
    }

    private String extractText(JsonNode body) {
        String msgType = JsonNodeUtils.asText(body, "msgtype");
        if ("text".equals(msgType)) {
            return JsonNodeUtils.asText(body, "text", "content");
        }
        if ("mixed".equals(msgType)) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : JsonNodeUtils.path(body, "mixed", "msg_item")) {
                if ("text".equals(JsonNodeUtils.asText(item, "msgtype"))) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(JsonNodeUtils.asText(item, "text", "content"));
                }
            }
            return sb.toString();
        }
        return "";
    }

    static List<ChannelImage> extractImages(JsonNode body) {
        List<ChannelImage> images = new ArrayList<>();
        if (JsonNodeUtils.isAbsent(body)) {
            return images;
        }
        String msgType = JsonNodeUtils.asText(body, "msgtype");
        if ("image".equals(msgType)) {
            addImage(images, JsonNodeUtils.path(body, "image"));
            return images;
        }
        if ("mixed".equals(msgType)) {
            for (JsonNode item : JsonNodeUtils.path(body, "mixed", "msg_item")) {
                if ("image".equals(JsonNodeUtils.asText(item, "msgtype"))) {
                    addImage(images, JsonNodeUtils.path(item, "image"));
                }
            }
        }
        return images;
    }

    private static void addImage(List<ChannelImage> images, JsonNode image) {
        if (JsonNodeUtils.isAbsent(image)) {
            return;
        }
        String url = JsonNodeUtils.firstText(image, "", "url", "pic_url");
        String aesKey = JsonNodeUtils.firstText(image, "", "aeskey", "aes_key");
        if (url == null || url.isBlank()) {
            return;
        }
        images.add(new ChannelImage(url, aesKey));
    }

    private Map<String, Object> toMap(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        if (node == null || !node.isObject()) {
            return map;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> e = fields.next();
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

    private final class WeiXinReplySink implements ChannelReplySink {
        private final String reqId;

        private WeiXinReplySink(String reqId) {
            this.reqId = reqId;
        }

        @Override
        public void replyText(String content) {
            String streamId = UUID.randomUUID().toString().replace("-", "");
            replyStream(streamId, content == null ? "" : content, true);
        }

        @Override
        public void replyStream(String streamId, String content, boolean finish) {
            ObjectNode root = JsonUtils.getObjectMapper().createObjectNode();
            root.put("cmd", WeiXinCmd.RESPOND_MSG);
            ObjectNode headers = root.putObject("headers");
            headers.put("req_id", reqId);
            ObjectNode body = root.putObject("body");
            body.put("msgtype", "stream");
            ObjectNode stream = body.putObject("stream");
            stream.put("id", streamId);
            stream.put("finish", finish);
            stream.put("content", content == null ? "" : content);
            send(root);
        }

        @Override
        public void replyWelcome(String content) {
            ObjectNode root = JsonUtils.getObjectMapper().createObjectNode();
            root.put("cmd", WeiXinCmd.RESPOND_WELCOME_MSG);
            ObjectNode headers = root.putObject("headers");
            headers.put("req_id", reqId);
            ObjectNode body = root.putObject("body");
            // 欢迎语支持文本；具体字段以官方文档为准，常见为 msgtype=text
            body.put("msgtype", "text");
            ObjectNode text = body.putObject("text");
            text.put("content", content == null ? "" : content);
            send(root);
        }

        private void send(ObjectNode root) {
            try {
                frameSender.accept(JsonUtils.getObjectMapper().writeValueAsString(root));
            } catch (Exception e) {
                throw new IllegalStateException("serialize WeiXin reply failed", e);
            }
        }
    }
}
