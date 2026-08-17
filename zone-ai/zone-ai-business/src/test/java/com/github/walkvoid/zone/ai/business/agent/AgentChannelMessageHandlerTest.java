package com.github.walkvoid.zone.ai.business.agent;

import com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelReplySink;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import com.github.walkvoid.zone.ai.business.channel.weixin.WeiXinMediaDownloader;
import com.github.walkvoid.zone.ai.business.tool.AppLogSearchTool;
import com.github.walkvoid.zone.ai.business.tool.RepoChangeTool;
import com.github.walkvoid.zone.ai.business.tool.RepoReadTool;
import com.github.walkvoid.zone.ai.business.tool.SqlQueryTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AgentChannelMessageHandlerTest {

    @Test
    void stripWeComMention() {
        assertEquals("测试一下", AgentChannelMessageHandler.stripMention("@俊卿的替身 测试一下"));
        assertEquals("查一下日志", AgentChannelMessageHandler.stripMention("查一下日志"));
        assertEquals("", AgentChannelMessageHandler.stripMention("  "));
    }

    @Test
    void resetCommandClearsGroupMemoryWithoutCallingModel() {
        OpenAiChatModel chatModel = mock(OpenAiChatModel.class);
        GroupChatMemoryService memory = new GroupChatMemoryService(new AgentMemoryProperties());
        memory.chatMemory().add("weixin:wr_group_1", List.of(
                new UserMessage("这笔融资"),
                new AssistantMessage("单号 123")));
        AgentChannelMessageHandler handler = new AgentChannelMessageHandler(
                chatModel,
                memory,
                mock(AppLogSearchTool.class),
                mock(SqlQueryTool.class),
                mock(RepoReadTool.class),
                mock(RepoChangeTool.class),
                mock(WeiXinMediaDownloader.class));
        RecordingSink sink = new RecordingSink();

        handler.onMessage(ChannelInboundMessage.builder()
                .channelType(ChannelType.WEIXIN)
                .chatId("wr_group_1")
                .chatType("group")
                .userId("zhangsan")
                .textContent("@机器人 新对话")
                .build(), sink);

        assertTrue(memory.get("weixin:wr_group_1").isEmpty());
        assertEquals(List.of("已清空本群对话上下文，可以开始新问题。"), sink.texts);
        assertTrue(sink.streams.isEmpty());
    }

    private static final class RecordingSink implements ChannelReplySink {
        private final List<String> texts = new ArrayList<>();
        private final List<String> streams = new ArrayList<>();

        @Override
        public void replyText(String content) {
            texts.add(content);
        }

        @Override
        public void replyStream(String streamId, String content, boolean finish) {
            streams.add(content);
        }
    }
}
