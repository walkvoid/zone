package com.github.walkvoid.zone.ai.business.channel.support;

import com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelReplySink;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 默认消息处理：回显文本，便于通道联调。
 * <p>
 * 业务侧可自行声明 {@link ChannelMessageHandler} Bean（建议 {@code @Primary}），
 * 本默认实现由 {@code ChannelConfiguration} 在缺失时注册。
 */
public class EchoChannelMessageHandler implements ChannelMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(EchoChannelMessageHandler.class);

    @Override
    public void onMessage(ChannelInboundMessage message, ChannelReplySink replySink) {
        String text = message.getTextContent();
        log.info("[{}] inbound user={}, chat={}, msgType={}, text={}",
                message.getChannelType(),
                message.getUserId(),
                message.getChatId(),
                message.getMsgType(),
                text);

        if (!StringUtils.hasText(text)) {
            replySink.replyText("暂不支持该消息类型，请发送文本。");
            return;
        }

        String streamId = UUID.randomUUID().toString().replace("-", "");
        replySink.replyStream(streamId, "收到：" + text.trim(), false);
        replySink.replyStream(streamId, "收到：" + text.trim() + "\n（默认 echo，后续可换成 Agent）", true);
    }

    @Override
    public void onEvent(ChannelType channelType, String eventType, String requestId,
                        ChannelReplySink replySink) {
        log.info("[{}] event={}, reqId={}", channelType, eventType, requestId);
    }
}
