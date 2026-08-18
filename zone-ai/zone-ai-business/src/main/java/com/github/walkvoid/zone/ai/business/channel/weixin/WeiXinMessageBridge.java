package com.github.walkvoid.zone.ai.business.channel.weixin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private final ObjectMapper mapper;
    private final ChannelMessageHandler messageHandler;
    private final String welcomeText;
    private final Consumer<String> frameSender;
    private final String configuredBotId;

    public WeiXinMessageBridge(ObjectMapper mapper,
                               ChannelMessageHandler messageHandler,
                               String welcomeText,
                               Consumer<String> frameSender,
                               String configuredBotId) {
        this.mapper = mapper;
        this.messageHandler = messageHandler;
        this.welcomeText = welcomeText;
        this.frameSender = frameSender;
        this.configuredBotId = configuredBotId;
    }

    public void onFrame(String text) {
        try {
            JsonNode root = mapper.readTree(text);
            String cmd = root.path("cmd").asText(null);
            if (cmd == null || cmd.isBlank()) {
                // 订阅/心跳响应通常只有 headers + errcode
                int errcode = root.path("errcode").asInt(Integer.MIN_VALUE);
                if (errcode != Integer.MIN_VALUE) {
                    log.info("WeiXin response without cmd, errcode={}, errmsg={}",
                            errcode, root.path("errmsg").asText());
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
                default -> log.info("WeiXin unhandled cmd={}, bodyKeys={}", cmd, fieldNames(root.path("body")));
            }
        } catch (Exception e) {
            log.error("WeiXin frame handle failed: {}", e.getMessage(), e);
        }
    }

    private void handleMsgCallback(JsonNode root) {
        String reqId = root.path("headers").path("req_id").asText();
        JsonNode body = root.path("body");
        log.info("WeiXin msg_callback reqId={}, aibotid={}, chatid={}, chattype={}, userid={}, msgtype={}",
                reqId,
                resolveBotId(body),
                body.path("chatid").asText(),
                body.path("chattype").asText(),
                body.path("from").path("userid").asText(),
                body.path("msgtype").asText());
        ChannelInboundMessage message = ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .requestId(reqId)
                .messageId(body.path("msgid").asText(null))
                .chatId(body.path("chatid").asText(null))
                .chatType(body.path("chattype").asText(null))
                .botId(resolveBotId(body))
                .userId(body.path("from").path("userid").asText(null))
                .msgType(body.path("msgtype").asText(null))
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
        String inbound = body.path("aibotid").asText(body.path("botid").asText(""));
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
        String reqId = root.path("headers").path("req_id").asText();
        JsonNode body = root.path("body");
        String eventType = body.path("eventtype").asText(body.path("event_type").asText("unknown"));
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
        String msgType = body.path("msgtype").asText("");
        if ("text".equals(msgType)) {
            return body.path("text").path("content").asText("");
        }
        if ("mixed".equals(msgType)) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : body.path("mixed").path("msg_item")) {
                if ("text".equals(item.path("msgtype").asText())) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(item.path("text").path("content").asText(""));
                }
            }
            return sb.toString();
        }
        return "";
    }

    static List<ChannelImage> extractImages(JsonNode body) {
        List<ChannelImage> images = new ArrayList<>();
        if (body == null || body.isMissingNode()) {
            return images;
        }
        String msgType = body.path("msgtype").asText("");
        if ("image".equals(msgType)) {
            addImage(images, body.path("image"));
            return images;
        }
        if ("mixed".equals(msgType)) {
            for (JsonNode item : body.path("mixed").path("msg_item")) {
                if ("image".equals(item.path("msgtype").asText())) {
                    addImage(images, item.path("image"));
                }
            }
        }
        return images;
    }

    private static void addImage(List<ChannelImage> images, JsonNode image) {
        if (image == null || image.isMissingNode() || image.isNull()) {
            return;
        }
        String url = image.path("url").asText(image.path("pic_url").asText(""));
        String aesKey = image.path("aeskey").asText(image.path("aes_key").asText(""));
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
            ObjectNode root = mapper.createObjectNode();
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
            ObjectNode root = mapper.createObjectNode();
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
                frameSender.accept(mapper.writeValueAsString(root));
            } catch (Exception e) {
                throw new IllegalStateException("serialize WeiXin reply failed", e);
            }
        }
    }
}
