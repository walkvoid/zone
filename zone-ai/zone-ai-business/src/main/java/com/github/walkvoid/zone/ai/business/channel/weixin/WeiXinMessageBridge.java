package com.github.walkvoid.zone.ai.business.channel.weixin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelReplySink;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
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

    public WeiXinMessageBridge(ObjectMapper mapper,
                               ChannelMessageHandler messageHandler,
                               String welcomeText,
                               Consumer<String> frameSender) {
        this.mapper = mapper;
        this.messageHandler = messageHandler;
        this.welcomeText = welcomeText;
        this.frameSender = frameSender;
    }

    public void onFrame(String text) {
        try {
            JsonNode root = mapper.readTree(text);
            String cmd = root.path("cmd").asText(null);
            if (cmd == null || cmd.isBlank()) {
                // 订阅/心跳响应通常只有 headers + errcode
                int errcode = root.path("errcode").asInt(Integer.MIN_VALUE);
                if (errcode != Integer.MIN_VALUE) {
                    log.debug("WeiXin response errcode={}, errmsg={}", errcode, root.path("errmsg").asText());
                }
                return;
            }

            switch (cmd) {
                case WeiXinCmd.MSG_CALLBACK -> handleMsgCallback(root);
                case WeiXinCmd.EVENT_CALLBACK -> handleEventCallback(root);
                case WeiXinCmd.PING -> log.trace("WeiXin ping echo ignored");
                default -> log.info("WeiXin unhandled cmd={}", cmd);
            }
        } catch (Exception e) {
            log.error("WeiXin frame handle failed: {}", e.getMessage(), e);
        }
    }

    private void handleMsgCallback(JsonNode root) {
        String reqId = root.path("headers").path("req_id").asText();
        JsonNode body = root.path("body");
        ChannelInboundMessage message = ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .requestId(reqId)
                .messageId(body.path("msgid").asText(null))
                .chatId(body.path("chatid").asText(null))
                .chatType(body.path("chattype").asText(null))
                .userId(body.path("from").path("userid").asText(null))
                .msgType(body.path("msgtype").asText(null))
                .textContent(extractText(body))
                .rawBody(toMap(body))
                .build();

        ChannelReplySink sink = new WeiXinReplySink(reqId);
        try {
            messageHandler.onMessage(message, sink);
        } catch (Exception e) {
            log.error("WeiXin message handler error", e);
            sink.replyText("处理消息时出错，请稍后重试。");
        }
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
