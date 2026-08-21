package com.github.walkvoid.zone.ai.agent;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 只把用户短文本和助手短回复写入记忆，丢掉图片字节、工具 JSON。
 */
final class TextOnlyChatMemoryRepository implements ChatMemoryRepository {

    private final InMemoryChatMemoryRepository delegate = new InMemoryChatMemoryRepository();
    private final int maxMessageChars;

    TextOnlyChatMemoryRepository(int maxMessageChars) {
        this.maxMessageChars = Math.max(1, maxMessageChars);
    }

    @Override
    public List<String> findConversationIds() {
        return delegate.findConversationIds();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return delegate.findByConversationId(conversationId);
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        List<Message> sanitized = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                Message kept = sanitize(message);
                if (kept != null) {
                    sanitized.add(kept);
                }
            }
        }
        delegate.saveAll(conversationId, sanitized);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        delegate.deleteByConversationId(conversationId);
    }

    private Message sanitize(Message message) {
        if (message == null || message.getMessageType() == null) {
            return null;
        }
        MessageType type = message.getMessageType();
        if (type == MessageType.USER) {
            return sanitizeUser(message);
        }
        if (type == MessageType.ASSISTANT) {
            return sanitizeAssistant(message);
        }
        return null;
    }

    private Message sanitizeUser(Message message) {
        String text = message.getText() == null ? "" : message.getText().trim();
        boolean hasImage = message instanceof UserMessage user && user.getMedia() != null && !user.getMedia().isEmpty();
        if (hasImage) {
            text = StringUtils.hasText(text) ? text + "（附图片）" : "（用户发了一张图片）";
        }
        text = truncate(text);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return UserMessage.builder().text(text).build();
    }

    private Message sanitizeAssistant(Message message) {
        if (message instanceof AssistantMessage assistant && assistant.hasToolCalls()) {
            return null;
        }
        String text = truncate(message.getText());
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return new AssistantMessage(text);
    }

    private String truncate(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.length() <= maxMessageChars) {
            return trimmed;
        }
        return trimmed.substring(0, maxMessageChars) + "…";
    }
}
