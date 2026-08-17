package com.github.walkvoid.zone.ai.business.agent;

import com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupConversationIdsTest {

    @Test
    void groupUsesChatIdRegardlessOfUser() {
        String fromZhang = GroupConversationIds.from(group("wr_group_1", "zhangsan"));
        String fromLi = GroupConversationIds.from(group("wr_group_1", "lisi"));
        assertEquals("weixin:wr_group_1", fromZhang);
        assertEquals(fromZhang, fromLi);
        assertEquals("weixin:wr_group_2", GroupConversationIds.from(group("wr_group_2", "zhangsan")));
    }

    @Test
    void singleChatUsesUserId() {
        assertEquals("weixin:single:zhangsan",
                GroupConversationIds.from(single("zhangsan")));
        assertEquals("weixin:single:zhangsan",
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

    private static ChannelInboundMessage group(String chatId, String userId) {
        return ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .chatId(chatId)
                .chatType("group")
                .userId(userId)
                .build();
    }

    private static ChannelInboundMessage single(String userId) {
        return ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .chatType("single")
                .userId(userId)
                .build();
    }
}
