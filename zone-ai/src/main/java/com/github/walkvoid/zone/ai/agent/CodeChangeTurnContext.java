package com.github.walkvoid.zone.ai.agent;

/**
 * 当前 Agent 一轮问答的改代码上下文。委托 {@link AgentTurnContext}。
 */
public final class CodeChangeTurnContext {

    private CodeChangeTurnContext() {
    }

    public static void open(Turn turn) {
        AgentTurnContext.open(turn, false);
    }

    public static Turn current() {
        return AgentTurnContext.current();
    }

    public static void close() {
        AgentTurnContext.close();
    }

    public record Turn(
            String conversationId,
            String sessionId,
            String turnNo,
            String messageId,
            String botId,
            String botCode,
            String chatId,
            String userId,
            String channelType,
            String requestText) {
    }
}
