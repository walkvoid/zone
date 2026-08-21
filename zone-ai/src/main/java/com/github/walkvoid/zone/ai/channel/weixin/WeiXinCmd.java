package com.github.walkvoid.zone.ai.channel.weixin;

/**
 * 企业微信智能机器人长连接命令常量。
 */
public final class WeiXinCmd {

    public static final String SUBSCRIBE = "aibot_subscribe";
    public static final String MSG_CALLBACK = "aibot_msg_callback";
    public static final String EVENT_CALLBACK = "aibot_event_callback";
    public static final String RESPOND_MSG = "aibot_respond_msg";
    public static final String RESPOND_WELCOME_MSG = "aibot_respond_welcome_msg";
    public static final String SEND_MSG = "aibot_send_msg";
    public static final String PING = "ping";

    private WeiXinCmd() {
    }
}
