package com.github.walkvoid.zone.ai.business.agent.audit;

import com.github.walkvoid.zone.ai.business.agent.CodeChangeTurnContext;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoWriteSupport;

/**
 * 审计队列事件。热路径只放已截断的字符串和不可变 snapshot。
 */
public final class AgentAuditEvent {

    public enum Type {
        TURN_START,
        TURN_FINISH,
        TOOL,
        CODE_CHANGE
    }

    private final Type type;
    private final CodeChangeTurnContext.Turn turn;
    private final boolean hasImage;
    private final int status;
    private final String finalAnswer;
    private final String errorMessage;
    private final long durationMs;
    private final int seq;
    private final String toolCode;
    private final String toolName;
    private final String requestJson;
    private final String responseJson;
    private final String responseSummary;
    private final boolean success;
    private final RepoWriteSupport.ApplyResult applied;

    private AgentAuditEvent(Builder builder) {
        this.type = builder.type;
        this.turn = builder.turn;
        this.hasImage = builder.hasImage;
        this.status = builder.status;
        this.finalAnswer = builder.finalAnswer;
        this.errorMessage = builder.errorMessage;
        this.durationMs = builder.durationMs;
        this.seq = builder.seq;
        this.toolCode = builder.toolCode;
        this.toolName = builder.toolName;
        this.requestJson = builder.requestJson;
        this.responseJson = builder.responseJson;
        this.responseSummary = builder.responseSummary;
        this.success = builder.success;
        this.applied = builder.applied;
    }

    public static AgentAuditEvent turnStart(CodeChangeTurnContext.Turn turn, boolean hasImage) {
        return new Builder(Type.TURN_START).turn(turn).hasImage(hasImage).build();
    }

    public static AgentAuditEvent turnFinish(CodeChangeTurnContext.Turn turn,
                                             int status,
                                             String finalAnswer,
                                             String errorMessage,
                                             long durationMs,
                                             boolean hasImage) {
        return new Builder(Type.TURN_FINISH)
                .turn(turn)
                .status(status)
                .finalAnswer(finalAnswer)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .hasImage(hasImage)
                .build();
    }

    public static AgentAuditEvent tool(CodeChangeTurnContext.Turn turn,
                                       int seq,
                                       String toolCode,
                                       String toolName,
                                       String requestJson,
                                       String responseJson,
                                       String responseSummary,
                                       boolean success,
                                       long durationMs,
                                       String errorMessage) {
        return new Builder(Type.TOOL)
                .turn(turn)
                .seq(seq)
                .toolCode(toolCode)
                .toolName(toolName)
                .requestJson(requestJson)
                .responseJson(responseJson)
                .responseSummary(responseSummary)
                .success(success)
                .durationMs(durationMs)
                .errorMessage(errorMessage)
                .build();
    }

    public static AgentAuditEvent codeChange(CodeChangeTurnContext.Turn turn,
                                             RepoWriteSupport.ApplyResult applied,
                                             String toolName) {
        return new Builder(Type.CODE_CHANGE)
                .turn(turn)
                .applied(applied)
                .toolName(toolName)
                .build();
    }

    public Type type() {
        return type;
    }

    public CodeChangeTurnContext.Turn turn() {
        return turn;
    }

    public boolean hasImage() {
        return hasImage;
    }

    public int status() {
        return status;
    }

    public String finalAnswer() {
        return finalAnswer;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public long durationMs() {
        return durationMs;
    }

    public int seq() {
        return seq;
    }

    public String toolCode() {
        return toolCode;
    }

    public String toolName() {
        return toolName;
    }

    public String requestJson() {
        return requestJson;
    }

    public String responseJson() {
        return responseJson;
    }

    public String responseSummary() {
        return responseSummary;
    }

    public boolean success() {
        return success;
    }

    public RepoWriteSupport.ApplyResult applied() {
        return applied;
    }

    private static final class Builder {
        private final Type type;
        private CodeChangeTurnContext.Turn turn;
        private boolean hasImage;
        private int status;
        private String finalAnswer;
        private String errorMessage;
        private long durationMs;
        private int seq;
        private String toolCode;
        private String toolName;
        private String requestJson;
        private String responseJson;
        private String responseSummary;
        private boolean success;
        private RepoWriteSupport.ApplyResult applied;

        private Builder(Type type) {
            this.type = type;
        }

        private Builder turn(CodeChangeTurnContext.Turn turn) {
            this.turn = turn;
            return this;
        }

        private Builder hasImage(boolean hasImage) {
            this.hasImage = hasImage;
            return this;
        }

        private Builder status(int status) {
            this.status = status;
            return this;
        }

        private Builder finalAnswer(String finalAnswer) {
            this.finalAnswer = finalAnswer;
            return this;
        }

        private Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        private Builder durationMs(long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        private Builder seq(int seq) {
            this.seq = seq;
            return this;
        }

        private Builder toolCode(String toolCode) {
            this.toolCode = toolCode;
            return this;
        }

        private Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        private Builder requestJson(String requestJson) {
            this.requestJson = requestJson;
            return this;
        }

        private Builder responseJson(String responseJson) {
            this.responseJson = responseJson;
            return this;
        }

        private Builder responseSummary(String responseSummary) {
            this.responseSummary = responseSummary;
            return this;
        }

        private Builder success(boolean success) {
            this.success = success;
            return this;
        }

        private Builder applied(RepoWriteSupport.ApplyResult applied) {
            this.applied = applied;
            return this;
        }

        private AgentAuditEvent build() {
            return new AgentAuditEvent(this);
        }
    }
}
