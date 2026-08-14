package com.github.walkvoid.zone.ai.business.channel.core;

/**
 * 通道回复出口。由各厂商 Client 实现，业务侧只依赖该接口。
 */
public interface ChannelReplySink {

    /**
     * 一次性文本/Markdown 回复（内部可按厂商能力转成流式 finish=true）。
     */
    void replyText(String content);

    /**
     * 流式回复。同一 streamId 多次调用会刷新同一条气泡；finish=true 结束。
     */
    void replyStream(String streamId, String content, boolean finish);

    /**
     * 欢迎语（进入会话等事件，部分厂商有时效要求）。
     */
    default void replyWelcome(String content) {
        replyText(content);
    }
}
