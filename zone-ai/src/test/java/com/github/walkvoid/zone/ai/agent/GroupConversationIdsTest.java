package com.github.walkvoid.zone.ai.agent;

import com.github.walkvoid.zone.ai.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.channel.core.ChannelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupConversationIdsTest {

    @Test
    void groupUsesChatIdAndBotIdRegardlessOfUser() {
        String fromZhang = GroupConversationIds.from(group("aib_1", "wr_group_1", "zhangsan"));
        String fromLi = GroupConversationIds.from(group("aib_1", "wr_group_1", "lisi"));
        assertEquals("weixin:aib_1:wr_group_1", fromZhang);
        assertEquals(fromZhang, fromLi);
        assertEquals("weixin:aib_1:wr_group_2",
                GroupConversationIds.from(group("aib_1", "wr_group_2", "zhangsan")));
        assertNotEquals(fromZhang,
                GroupConversationIds.from(group("aib_2", "wr_group_1", "zhangsan")));
    }

    @Test
    void singleChatUsesBotAndUserId() {
        assertEquals("weixin:aib_1:single:zhangsan",
                GroupConversationIds.from(single("aib_1", "zhangsan")));
        assertEquals("weixin:unknown:single:zhangsan",
                GroupConversationIds.from(ChannelInboundMessage.builder()
                        .channelType(ChannelType.WEIXIN)
                        .chatId("  ")
                        .userId("zhangsan")
                        .build()));
    }

    @Test
    void resetCommands() {
        assertTrue(GroupConversationIds.isResetCommand("新对话"));
        assertTrue(GroupConversationIds.isResetCommand(" 清空上下文。"));
        assertTrue(GroupConversationIds.isResetCommand("重新开始"));
        assertTrue(GroupConversationIds.isResetCommand("新开一轮！"));
        assertFalse(GroupConversationIds.isResetCommand("新对话吧"));
        assertFalse(GroupConversationIds.isResetCommand("这笔融资什么状态"));
        assertFalse(GroupConversationIds.isResetCommand(""));
    }

    private static ChannelInboundMessage group(String botId, String chatId, String userId) {
        return ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .botId(botId)
                .chatId(chatId)
                .chatType("group")
                .userId(userId)
                .build();
    }

    private static ChannelInboundMessage single(String botId, String userId) {
        return ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .botId(botId)
                .chatType("single")
                .userId(userId)
                .build();
    }
}
