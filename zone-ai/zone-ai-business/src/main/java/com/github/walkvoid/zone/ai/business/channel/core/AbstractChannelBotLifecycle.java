package com.github.walkvoid.zone.ai.business.channel.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通道客户端生命周期基类。
 * <p>
 * 接入 Spring {@link SmartLifecycle}：容器启动后自动 start，关闭时 stop。
 * 企微 / 飞书等实现只需实现 {@link #doStart()} / {@link #doStop()}。
 */
public abstract class AbstractChannelBotLifecycle implements SmartLifecycle, ChannelBotClient {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<ChannelConnectionState> state =
            new AtomicReference<>(ChannelConnectionState.STOPPED);

    @Override
    public final void start() {
        if (!isEnabled()) {
            log.info("[{}] channel disabled, skip start", channelType());
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.warn("[{}] channel already running", channelType());
            return;
        }
        updateState(ChannelConnectionState.STARTING);
        try {
            doStart();
            log.info("[{}] channel started", channelType());
        } catch (Exception e) {
            running.set(false);
            updateState(ChannelConnectionState.FAILED);
            log.error("[{}] channel start failed", channelType(), e);
            throw new IllegalStateException(channelType() + " channel start failed", e);
        }
    }

    @Override
    public final void stop() {
        if (!running.compareAndSet(true, false)) {
            updateState(ChannelConnectionState.STOPPED);
            return;
        }
        updateState(ChannelConnectionState.STOPPING);
        try {
            doStop();
            log.info("[{}] channel stopped", channelType());
        } catch (Exception e) {
            log.error("[{}] channel stop error", channelType(), e);
        } finally {
            updateState(ChannelConnectionState.STOPPED);
        }
    }

    @Override
    public final void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public final boolean isRunning() {
        return running.get();
    }

    /**
     * 始终进入 start()：未启用时打 skip 日志，避免配置绑失败时完全静默。
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * 较晚启动、较早停止，避免拖慢核心 Web/Dubbo。
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }

    @Override
    public ChannelConnectionState connectionState() {
        return state.get();
    }

    protected void updateState(ChannelConnectionState newState) {
        ChannelConnectionState old = state.getAndSet(newState);
        if (old != newState) {
            log.info("[{}] state {} -> {}", channelType(), old, newState);
        }
    }

    /**
     * 生命周期仍要求运行（用于断线重连判断）。
     */
    protected boolean shouldKeepRunning() {
        return running.get();
    }

    /**
     * 子类在「启用但暂不真正建连」等场景可调用，将运行标记清掉。
     */
    protected final void markNotRunning(ChannelConnectionState newState) {
        running.set(false);
        updateState(newState);
    }

    /**
     * 建立连接 / 订阅 / 启动心跳等。
     */
    protected abstract void doStart() throws Exception;

    /**
     * 停止心跳、关闭连接、释放资源。
     */
    protected abstract void doStop() throws Exception;
}
