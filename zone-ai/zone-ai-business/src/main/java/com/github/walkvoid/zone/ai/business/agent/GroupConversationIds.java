package com.github.walkvoid.zone.ai.business.agent;

import com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 * 群会话 Key 与清空口令。群聊整群一份上下文，单聊按人一份。
 */
public final class GroupConversationIds {

    private static final Set<String> RESET_COMMANDS = Set.of(
            "新对话",
            "清空上下文",
            "重新开始",
            "新开一轮"
    );

    private GroupConversationIds() {
    }

    public static String from(ChannelInboundMessage message) {
        String channel = message.getChannelType() == null
                ? "unknown"
                : message.getChannelType().name().toLowerCase(Locale.ROOT);
        if (isSingleChat(message)) {
            return channel + ":single:" + orUnknown(message.getUserId());
        }
        return channel + ":" + message.getChatId().trim();
    }

    public static boolean isResetCommand(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        return RESET_COMMANDS.contains(stripTrailingPunctuation(text.trim()));
    }

    static boolean isSingleChat(ChannelInboundMessage message) {
        String chatType = message.getChatType();
        if (StringUtils.hasText(chatType) && "single".equalsIgnoreCase(chatType.trim())) {
            return true;
        }
        return !StringUtils.hasText(message.getChatId());
    }

    private static String orUnknown(String value) {
        return StringUtils.hasText(value) ? value.trim() : "unknown";
    }

    private static String stripTrailingPunctuation(String text) {
        int end = text.length();
        while (end > 0 && isTrailingPunctuation(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end).trim();
    }

    private static boolean isTrailingPunctuation(char ch) {
        return ch == '。' || ch == '！' || ch == '？' || ch == '.' || ch == '!' || ch == '?'
                || ch == '，' || ch == ',' || Character.isWhitespace(ch);
    }
}
