package com.github.walkvoid.zone.ai.business.channel.weixin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.zone.ai.business.channel.core.AbstractChannelBotLifecycle;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelConnectionState;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelProperties;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 企业微信智能机器人长连接客户端。
 * <p>
 * 通过 {@link AbstractChannelBotLifecycle} 接入 Spring 生命周期：
 * 容器启动后建连并 {@code aibot_subscribe}，关闭时停心跳并断开。
 */
@Component
public class WeiXinAiBotClient extends AbstractChannelBotLifecycle {

    private final ChannelProperties channelProperties;
    private final ChannelMessageHandler messageHandler;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    private final AtomicBoolean subscribed = new AtomicBoolean(false);
    private final AtomicLong reconnectDelayMs = new AtomicLong();
    private final StringBuilder textBuffer = new StringBuilder();

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> heartbeatFuture;
    private ScheduledFuture<?> reconnectFuture;
    private WeiXinMessageBridge bridge;

    public WeiXinAiBotClient(ChannelProperties channelProperties,
                             ChannelMessageHandler messageHandler,
                             ObjectMapper objectMapper) {
        this.channelProperties = channelProperties;
        this.messageHandler = messageHandler;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.WEIXIN;
    }

    @Override
    public boolean isEnabled() {
        WeiXinAiBotProperties p = channelProperties.getWeixin();
        return channelProperties.isEnabled() && p.isEnabled() && p.hasCredentials();
    }

    @Override
    protected void doStart() {
        System.out.println("=======开始启动微信WeiXin========");
        WeiXinAiBotProperties props = channelProperties.getWeixin();
        if (!props.hasCredentials()) {
            throw new IllegalStateException("WeiXin botId/secret is empty");
        }
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "weixin-aibot-scheduler");
            t.setDaemon(true);
            return t;
        });
        this.reconnectDelayMs.set(props.getReconnectInitialMs());
        this.bridge = new WeiXinMessageBridge(
                objectMapper,
                messageHandler,
                props.getWelcomeText(),
                this::sendTextSafe);
        connectAsync();
    }

    @Override
    protected void doStop() {
        cancelReconnect();
        stopHeartbeat();
        WebSocket ws = webSocketRef.getAndSet(null);
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown").join();
            } catch (Exception e) {
                log.warn("WeiXin websocket close error: {}", e.getMessage());
            }
        }
        subscribed.set(false);
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void connectAsync() {
        if (!shouldKeepRunning()) {
            return;
        }
        updateState(ChannelConnectionState.CONNECTING);
        WeiXinAiBotProperties props = channelProperties.getWeixin();
        URI uri = URI.create(props.getWsUrl());
        log.info("WeiXin connecting to {}", props.getWsUrl());

        httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .buildAsync(uri, new WeiXinListener())
                .whenComplete((ws, error) -> {
                    if (error != null) {
                        log.error("WeiXin connect failed: {}", error.getMessage());
                        updateState(ChannelConnectionState.FAILED);
                        scheduleReconnect();
                        return;
                    }
                    webSocketRef.set(ws);
                    updateState(ChannelConnectionState.SUBSCRIBING);
                    sendSubscribe(props);
                });
    }

    private void sendSubscribe(WeiXinAiBotProperties props) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("cmd", WeiXinCmd.SUBSCRIBE);
            root.putObject("headers").put("req_id", newReqId());
            ObjectNode body = root.putObject("body");
            body.put("bot_id", props.getBotId());
            body.put("secret", props.getSecret());
            sendTextSafe(objectMapper.writeValueAsString(root));
            log.info("WeiXin aibot_subscribe sent, botId={}", props.getBotId());
        } catch (Exception e) {
            log.error("WeiXin subscribe send failed", e);
            scheduleReconnect();
        }
    }

    private void onSubscribed() {
        subscribed.set(true);
        reconnectDelayMs.set(channelProperties.getWeixin().getReconnectInitialMs());
        updateState(ChannelConnectionState.READY);
        startHeartbeat();
        log.info("WeiXin channel READY");
    }

    private void startHeartbeat() {
        stopHeartbeat();
        long interval = channelProperties.getWeixin().getHeartbeatIntervalMs();
        heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            if (!shouldKeepRunning() || !subscribed.get()) {
                return;
            }
            try {
                ObjectNode root = objectMapper.createObjectNode();
                root.put("cmd", WeiXinCmd.PING);
                root.putObject("headers").put("req_id", newReqId());
                sendTextSafe(objectMapper.writeValueAsString(root));
            } catch (Exception e) {
                log.warn("WeiXin ping failed: {}", e.getMessage());
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
            heartbeatFuture = null;
        }
    }

    private void scheduleReconnect() {
        if (!shouldKeepRunning()) {
            return;
        }
        cancelReconnect();
        stopHeartbeat();
        subscribed.set(false);
        WebSocket old = webSocketRef.getAndSet(null);
        if (old != null) {
            try {
                old.abort();
            } catch (Exception ignored) {
                // ignore
            }
        }
        updateState(ChannelConnectionState.RECONNECTING);
        long delay = reconnectDelayMs.get();
        long max = channelProperties.getWeixin().getReconnectMaxMs();
        long next = Math.min(Math.max(delay * 2, delay), max);
        reconnectDelayMs.set(next <= 0 ? channelProperties.getWeixin().getReconnectInitialMs() : next);
        log.info("WeiXin reconnect in {} ms", delay);
        reconnectFuture = scheduler.schedule(this::connectAsync, delay, TimeUnit.MILLISECONDS);
    }

    private void cancelReconnect() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
    }

    private void sendTextSafe(String text) {
        WebSocket ws = webSocketRef.get();
        if (ws == null) {
            log.warn("WeiXin send skipped, websocket not ready");
            return;
        }
        try {
            ws.sendText(text, true).join();
        } catch (Exception e) {
            log.error("WeiXin send failed: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private static String newReqId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private final class WeiXinListener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("WeiXin websocket opened");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String payload = textBuffer.toString();
                textBuffer.setLength(0);
                System.out.println("=======WeiXin onText len=" + payload.length() + "=======");
                log.info("WeiXin onText last=true, len={}", payload.length());
                handlePayload(payload);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            String payload = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("=======WeiXin onBinary last=" + last + " len=" + payload.length() + "=======");
            log.info("WeiXin onBinary last={}, len={}", last, payload.length());
            if (last && StringUtils.hasText(payload)) {
                handlePayload(payload);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("WeiXin websocket closed: code={}, reason={}", statusCode, reason);
            subscribed.set(false);
            if (shouldKeepRunning()) {
                scheduleReconnect();
            } else {
                updateState(ChannelConnectionState.STOPPED);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("WeiXin websocket error: {}", error.getMessage());
            subscribed.set(false);
            if (shouldKeepRunning()) {
                scheduleReconnect();
            } else {
                updateState(ChannelConnectionState.FAILED);
            }
        }

        private void handlePayload(String payload) {
            if (!StringUtils.hasText(payload)) {
                return;
            }
            String preview = payload.length() > 500 ? payload.substring(0, 500) + "..." : payload;
            preview = preview.replaceAll("(?i)\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***\"");
            System.out.println("=======WeiXin inbound=======\n" + preview);
            log.info("WeiXin inbound: {}", preview);
            // 订阅成功响应：无 cmd，errcode=0
            try {
                var root = objectMapper.readTree(payload);
                String cmd = root.path("cmd").asText("");
                log.info("WeiXin inbound cmd='{}', errcode={}, subscribed={}",
                        cmd, root.path("errcode").asInt(Integer.MIN_VALUE), subscribed.get());
                if (!root.has("cmd") && root.path("errcode").asInt(-1) == 0 && !subscribed.get()) {
                    onSubscribed();
                    return;
                }
                if (!root.has("cmd") && root.path("errcode").asInt(0) != 0 && !subscribed.get()) {
                    log.error("WeiXin subscribe failed: {}", payload);
                    scheduleReconnect();
                    return;
                }
            } catch (Exception e) {
                log.warn("WeiXin payload parse probe failed: {}", e.getMessage());
            }
            if (bridge != null) {
                bridge.onFrame(payload);
            } else {
                log.warn("WeiXin inbound dropped, bridge is null");
            }
        }
    }
}
