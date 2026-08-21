package com.github.walkvoid.zone.ai.agent;

import com.github.walkvoid.zone.ai.agent.audit.AgentAuditEvent;
import com.github.walkvoid.zone.ai.agent.audit.AgentAuditQueue;
import com.github.walkvoid.zone.ai.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.channel.core.ChannelProperties;
import com.github.walkvoid.zone.ai.channel.core.ChannelReplySink;
import com.github.walkvoid.zone.ai.channel.weixin.WeiXinMediaDownloader;
import com.github.walkvoid.zone.ai.db.entity.AiBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 企微/飞书消息走大模型；prompt 与工具集按 {@code ai_bot_config} 按 bot 隔离。
 */
@Primary
@Component
public class AgentChannelMessageHandler implements ChannelMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentChannelMessageHandler.class);
    private static final int MAX_REPLY_CHARS = 3500;
    private static final long TIMEOUT_SECONDS = 120;
    private static final String RESET_REPLY = "已清空本群对话上下文，可以开始新问题。";

    private final ChatClient chatClient;
    private final GroupChatMemoryService groupChatMemoryService;
    private final AiBotConfigService aiBotConfigService;
    private final AgentToolRegistry agentToolRegistry;
    private final ChannelProperties channelProperties;
    private final WeiXinMediaDownloader mediaDownloader;
    private final AgentAuditQueue auditQueue;
    private final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "weixin-agent");
        t.setDaemon(true);
        return t;
    });

    public AgentChannelMessageHandler(OpenAiChatModel chatModel,
                                      GroupChatMemoryService groupChatMemoryService,
                                      AiBotConfigService aiBotConfigService,
                                      AgentToolRegistry agentToolRegistry,
                                      ChannelProperties channelProperties,
                                      WeiXinMediaDownloader mediaDownloader) {
        this(chatModel, groupChatMemoryService, aiBotConfigService, agentToolRegistry,
                channelProperties, mediaDownloader, null);
    }

    @Autowired
    public AgentChannelMessageHandler(OpenAiChatModel chatModel,
                                      GroupChatMemoryService groupChatMemoryService,
                                      AiBotConfigService aiBotConfigService,
                                      AgentToolRegistry agentToolRegistry,
                                      ChannelProperties channelProperties,
                                      WeiXinMediaDownloader mediaDownloader,
                                      ObjectProvider<AgentAuditQueue> auditQueue) {
        this.groupChatMemoryService = groupChatMemoryService;
        this.aiBotConfigService = aiBotConfigService;
        this.agentToolRegistry = agentToolRegistry;
        this.channelProperties = channelProperties;
        this.mediaDownloader = mediaDownloader;
        this.auditQueue = auditQueue == null ? null : auditQueue.getIfAvailable();
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(groupChatMemoryService.advisor())
                .build();
        log.info("Agent handler ready, prompt/tools loaded per ai_bot_config");
    }

    @Override
    public void onMessage(ChannelInboundMessage message, ChannelReplySink replySink) {
        String text = stripMention(message.getTextContent());
        boolean hasImages = message.hasImages();
        log.info("[{}] agent inbound bot={}, user={}, chat={}, conversationId={}, msgType={}, text={}, images={}",
                message.getChannelType(), message.getBotId(), message.getUserId(), message.getChatId(),
                GroupConversationIds.from(message),
                message.getMsgType(), text, message.getImages().size());

        if (!StringUtils.hasText(text) && !hasImages) {
            replySink.replyText("暂不支持该消息类型，请发送文本或图片。");
            return;
        }

        String conversationId = GroupConversationIds.from(message);
        if (GroupConversationIds.isResetCommand(text)) {
            groupChatMemoryService.runExclusive(conversationId, () -> {
                groupChatMemoryService.clear(conversationId);
                return null;
            });
            log.info("[{}] reset conversation {}", message.getChannelType(), conversationId);
            replySink.replyText(RESET_REPLY);
            return;
        }

        String streamId = UUID.randomUUID().toString().replace("-", "");
        replySink.replyStream(streamId, hasImages ? "正在识别图片…" : "正在分析…", false);

        CompletableFuture
                .supplyAsync(() -> askModel(text, message), executor)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .whenComplete((answer, error) -> {
                    try {
                        if (error != null) {
                            log.error("agent reply failed", unwrap(error));
                            replySink.replyStream(streamId, friendlyError(error), true);
                            return;
                        }
                        replySink.replyStream(streamId, truncate(answer), true);
                    } catch (Exception e) {
                        log.error("agent finish reply failed", e);
                        try {
                            replySink.replyStream(streamId, "回复发送失败，请稍后重试。", true);
                        } catch (Exception ignored) {
                            // 保证不再抛出
                        }
                    }
                });
    }

    private String askModel(String userText, ChannelInboundMessage message) {
        List<Media> media = List.of();
        if (message.hasImages()) {
            try {
                media = toMedia(mediaDownloader.download(message.getImages()));
            } catch (Exception e) {
                log.warn("download inbound images failed: {}", e.getMessage());
                return "图片下载失败，请再发一次，或改成文字描述。";
            }
        }
        String promptText = StringUtils.hasText(userText)
                ? userText
                : "请看这张图片，识别其中的文字、报错、traceId 或业务单号，并按排障助手规则回答。";
        String conversationId = GroupConversationIds.from(message);
        AiBotConfig bot = aiBotConfigService.findByBotId(message.getBotId(), channelProperties.getWeixin());
        String systemPrompt = AiBotConfigService.resolvePrompt(bot);
        List<AgentToolCode> toolCodes = AiBotConfigService.resolveTools(bot);
        Object[] tools = agentToolRegistry.resolve(toolCodes);
        log.info("agent askModel start, conversationId={}, botId={}, tools={}, text={}, images={}",
                conversationId,
                message.getBotId(),
                toolCodes.stream().map(AgentToolCode::code).collect(Collectors.joining(",")),
                promptText,
                media.size());
        Media[] mediaArr = media.toArray(Media[]::new);
        String turnNo = UUID.randomUUID().toString().replace("-", "");
        return groupChatMemoryService.runExclusive(conversationId, () -> {
            String sessionId = groupChatMemoryService.resolveSessionId(conversationId);
            CodeChangeTurnContext.Turn turn = new CodeChangeTurnContext.Turn(
                    conversationId,
                    sessionId,
                    turnNo,
                    message.getMessageId(),
                    message.getBotId(),
                    bot == null ? null : bot.getBotCode(),
                    message.getChatId(),
                    message.getUserId(),
                    message.getChannelType() == null ? null : message.getChannelType().name(),
                    promptText);
            AgentTurnContext.open(turn, mediaArr.length > 0);
            offerAudit(AgentAuditEvent.turnStart(turn, mediaArr.length > 0));
            try {
                var spec = chatClient.prompt()
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .system(systemPrompt)
                        .user(u -> {
                            u.text(promptText);
                            if (mediaArr.length > 0) {
                                u.media(mediaArr);
                            }
                        });
                if (tools.length > 0) {
                    spec.tools(tools);
                }
                String content = spec.call().content();
                String answer = StringUtils.hasText(content) ? content.trim()
                        : "我这边没有生成到有效回答，请换个说法再问一次。";
                log.info("agent askModel done, conversationId={}, turnNo={}, replyChars={}",
                        conversationId, turnNo, answer.length());
                offerAudit(AgentAuditEvent.turnFinish(
                        turn,
                        AgentTurnContext.STATUS_SUCCESS,
                        answer,
                        null,
                        AgentTurnContext.currentState() == null ? 0L : AgentTurnContext.currentState().elapsedMs(),
                        mediaArr.length > 0));
                return answer;
            } catch (RuntimeException e) {
                offerAudit(AgentAuditEvent.turnFinish(
                        turn,
                        AgentTurnContext.STATUS_FAILED,
                        null,
                        e.getMessage(),
                        AgentTurnContext.currentState() == null ? 0L : AgentTurnContext.currentState().elapsedMs(),
                        mediaArr.length > 0));
                throw e;
            } finally {
                AgentTurnContext.close();
            }
        });
    }

    private void offerAudit(AgentAuditEvent event) {
        if (auditQueue != null) {
            auditQueue.offer(event);
        }
    }

    private static List<Media> toMedia(List<WeiXinMediaDownloader.DownloadedImage> images) {
        List<Media> result = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            WeiXinMediaDownloader.DownloadedImage image = images.get(i);
            int index = i;
            ByteArrayResource resource = new ByteArrayResource(image.bytes()) {
                @Override
                public String getFilename() {
                    return "wecom-image-" + index + mimeSuffix(image.mimeType().toString());
                }
            };
            result.add(new Media(image.mimeType(), resource));
        }
        return result;
    }

    private static String mimeSuffix(String mime) {
        if (mime == null) {
            return ".jpg";
        }
        if (mime.contains("png")) {
            return ".png";
        }
        if (mime.contains("gif")) {
            return ".gif";
        }
        if (mime.contains("webp")) {
            return ".webp";
        }
        return ".jpg";
    }

    static String stripMention(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        return raw.trim().replaceFirst("^@\\S+\\s+", "").trim();
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= MAX_REPLY_CHARS) {
            return text;
        }
        return text.substring(0, MAX_REPLY_CHARS) + "\n…（已截断）";
    }

    private static String friendlyError(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof TimeoutException) {
            return "分析超时了，请缩小范围后再问，例如带上 traceId 或业务单号。";
        }
        return "刚才调用模型失败了，请稍后重试。";
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) {
            if (current instanceof TimeoutException) {
                return current;
            }
            current = current.getCause();
        }
        return current;
    }
}
