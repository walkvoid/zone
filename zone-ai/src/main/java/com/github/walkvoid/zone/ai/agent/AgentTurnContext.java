package com.github.walkvoid.zone.ai.agent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 当前 Agent 一轮问答上下文。与 {@code ChatClient.call()} 同线程。
 * {@link CodeChangeTurnContext} 转发到这里，改代码历史与对话日志共用 {@code turnNo}。
 */
public final class AgentTurnContext {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_TIMEOUT = 3;

    private static final ThreadLocal<State> CURRENT = new ThreadLocal<>();

    private AgentTurnContext() {
    }

    public static void open(CodeChangeTurnContext.Turn turn, boolean hasImage) {
        CURRENT.set(new State(turn, hasImage, System.nanoTime()));
    }

    public static State currentState() {
        return CURRENT.get();
    }

    public static CodeChangeTurnContext.Turn current() {
        State state = CURRENT.get();
        return state == null ? null : state.turn;
    }

    public static void close() {
        CURRENT.remove();
    }

    public static final class State {
        public final CodeChangeTurnContext.Turn turn;
        public final boolean hasImage;
        public final long startNanos;
        private final AtomicInteger seq = new AtomicInteger();

        State(CodeChangeTurnContext.Turn turn, boolean hasImage, long startNanos) {
            this.turn = turn;
            this.hasImage = hasImage;
            this.startNanos = startNanos;
        }

        public int nextSeq() {
            return seq.incrementAndGet();
        }

        public long elapsedMs() {
            return Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
        }
    }
}
