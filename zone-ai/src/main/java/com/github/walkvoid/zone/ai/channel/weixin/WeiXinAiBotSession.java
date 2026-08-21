package com.github.walkvoid.zone.ai.channel.weixin;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.utils.JsonNodeUtils;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.channel.core.ChannelConnectionState;
import com.github.walkvoid.zone.ai.channel.core.ChannelMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.function.BooleanSupplier;

/**
 * 单个企微 aibot 的 WebSocket 会话。同一 botId 全局只能有一条连接。
 */
final class WeiXinAiBotSession {

    private static final Logger log = LoggerFactory.getLogger(WeiXinAiBotSession.class);

    private final String botId;
    private final String secret;
    private final String welcomeText;
    private final String wsUrl;
    private final long heartbeatIntervalMs;
    private final long reconnectInitialMs;
    private final long reconnectMaxMs;
    private final ChannelMessageHandler messageHandler;
    private final HttpClient httpClient;
    private final BooleanSupplier keepRunning;
    private final Runnable onReady;

    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    private final AtomicBoolean subscribed = new AtomicBoolean(false);
    private final AtomicReference<ChannelConnectionState> state =
            new AtomicReference<>(ChannelConnectionState.STOPPED);
    private final AtomicLong reconnectDelayMs = new AtomicLong();
    private final StringBuilder textBuffer = new StringBuilder();

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> heartbeatFuture;
    private ScheduledFuture<?> reconnectFuture;
    private WeiXinMessageBridge bridge;

    WeiXinAiBotSession(String botId,
                       String secret,
                       String welcomeText,
                       WeiXinAiBotProperties transport,
                       ChannelMessageHandler messageHandler,
                       HttpClient httpClient,
                       BooleanSupplier keepRunning,
                       Runnable onReady) {
        this.botId = botId;
        this.secret = secret;
        this.welcomeText = welcomeText;
        this.wsUrl = transport.getWsUrl();
        this.heartbeatIntervalMs = transport.getHeartbeatIntervalMs();
        this.reconnectInitialMs = transport.getReconnectInitialMs();
        this.reconnectMaxMs = transport.getReconnectMaxMs();
        this.messageHandler = messageHandler;
        this.httpClient = httpClient;
        this.keepRunning = keepRunning;
        this.onReady = onReady;
    }

    String botId() {
        return botId;
    }

    ChannelConnectionState state() {
        return state.get();
    }

    boolean isReady() {
        return subscribed.get() && state.get() == ChannelConnectionState.READY;
    }

    void start() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "weixin-aibot-" + shortId(botId));
            t.setDaemon(true);
            return t;
        });
        this.reconnectDelayMs.set(reconnectInitialMs);
        this.bridge = new WeiXinMessageBridge(
                messageHandler,
                welcomeText,
                this::sendTextSafe,
                botId);
        connectAsync();
    }

    void stop() {
        cancelReconnect();
        stopHeartbeat();
        WebSocket ws = webSocketRef.getAndSet(null);
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown").join();
            } catch (Exception e) {
                log.warn("WeiXin[{}] websocket close error: {}", botId, e.getMessage());
            }
        }
        subscribed.set(false);
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        updateState(ChannelConnectionState.STOPPED);
    }

    private void connectAsync() {
        if (!keepRunning.getAsBoolean()) {
            return;
        }
        updateState(ChannelConnectionState.CONNECTING);
        log.info("WeiXin[{}] connecting to {}", botId, wsUrl);
        httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .buildAsync(URI.create(wsUrl), new WeiXinListener())
                .whenComplete((ws, error) -> {
                    if (error != null) {
                        log.error("WeiXin[{}] connect failed: {}", botId, error.getMessage());
                        updateState(ChannelConnectionState.FAILED);
                        scheduleReconnect();
                        return;
                    }
                    webSocketRef.set(ws);
                    updateState(ChannelConnectionState.SUBSCRIBING);
                    sendSubscribe();
                });
    }

    private void sendSubscribe() {
        try {
            ObjectNode root = JsonUtils.getObjectMapper().createObjectNode();
            root.put("cmd", WeiXinCmd.SUBSCRIBE);
            root.putObject("headers").put("req_id", newReqId());
            ObjectNode body = root.putObject("body");
            body.put("bot_id", botId);
            body.put("secret", secret);
            sendTextSafe(JsonUtils.getObjectMapper().writeValueAsString(root));
            log.info("WeiXin[{}] aibot_subscribe sent", botId);
        } catch (Exception e) {
            log.error("WeiXin[{}] subscribe send failed", botId, e);
            scheduleReconnect();
        }
    }

    private void onSubscribed() {
        subscribed.set(true);
        reconnectDelayMs.set(reconnectInitialMs);
        updateState(ChannelConnectionState.READY);
        startHeartbeat();
        log.info("WeiXin[{}] channel READY", botId);
        if (onReady != null) {
            onReady.run();
        }
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            if (!keepRunning.getAsBoolean() || !subscribed.get()) {
                return;
            }
            try {
                ObjectNode root = JsonUtils.getObjectMapper().createObjectNode();
                root.put("cmd", WeiXinCmd.PING);
                root.putObject("headers").put("req_id", newReqId());
                sendTextSafe(JsonUtils.getObjectMapper().writeValueAsString(root));
            } catch (Exception e) {
                log.warn("WeiXin[{}] ping failed: {}", botId, e.getMessage());
            }
        }, heartbeatIntervalMs, heartbeatIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
            heartbeatFuture = null;
        }
    }

    private void scheduleReconnect() {
        if (!keepRunning.getAsBoolean()) {
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
        long next = Math.min(Math.max(delay * 2, delay), reconnectMaxMs);
        reconnectDelayMs.set(next <= 0 ? reconnectInitialMs : next);
        log.info("WeiXin[{}] reconnect in {} ms", botId, delay);
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
            log.warn("WeiXin[{}] send skipped, websocket not ready", botId);
            return;
        }
        try {
            ws.sendText(text, true).join();
        } catch (Exception e) {
            log.error("WeiXin[{}] send failed: {}", botId, e.getMessage());
            scheduleReconnect();
        }
    }

    private void updateState(ChannelConnectionState newState) {
        ChannelConnectionState old = state.getAndSet(newState);
        if (old != newState) {
            log.info("WeiXin[{}] state {} -> {}", botId, old, newState);
        }
    }

    private static String newReqId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String shortId(String botId) {
        if (!StringUtils.hasText(botId) || botId.length() <= 12) {
            return botId == null ? "bot" : botId;
        }
        return botId.substring(0, 12);
    }

    private final class WeiXinListener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("WeiXin[{}] websocket opened", botId);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String payload = textBuffer.toString();
                textBuffer.setLength(0);
                log.info("WeiXin[{}] onText last=true, len={}", botId, payload.length());
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
            log.info("WeiXin[{}] onBinary last={}, len={}", botId, last, payload.length());
            if (last && StringUtils.hasText(payload)) {
                handlePayload(payload);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("WeiXin[{}] websocket closed: code={}, reason={}", botId, statusCode, reason);
            subscribed.set(false);
            if (keepRunning.getAsBoolean()) {
                scheduleReconnect();
            } else {
                updateState(ChannelConnectionState.STOPPED);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("WeiXin[{}] websocket error: {}", botId, error.getMessage());
            subscribed.set(false);
            if (keepRunning.getAsBoolean()) {
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
            log.info("WeiXin[{}] inbound: {}", botId, preview);
            try {
                var root = JsonUtils.getObjectMapper().readTree(payload);
                String cmd = JsonNodeUtils.asText(root, "cmd");
                log.info("WeiXin[{}] inbound cmd='{}', errcode={}, subscribed={}",
                        botId, cmd, JsonNodeUtils.asInt(root, Integer.MIN_VALUE, "errcode"), subscribed.get());
                if (!JsonNodeUtils.has(root, "cmd") && JsonNodeUtils.asInt(root, -1, "errcode") == 0 && !subscribed.get()) {
                    onSubscribed();
                    return;
                }
                if (!JsonNodeUtils.has(root, "cmd") && JsonNodeUtils.asInt(root, 0, "errcode") != 0 && !subscribed.get()) {
                    log.error("WeiXin[{}] subscribe failed: {}", botId, payload);
                    scheduleReconnect();
                    return;
                }
            } catch (Exception e) {
                log.warn("WeiXin[{}] payload parse probe failed: {}", botId, e.getMessage());
            }
            if (bridge != null) {
                bridge.onFrame(payload);
            } else {
                log.warn("WeiXin[{}] inbound dropped, bridge is null", botId);
            }
        }
    }
}
