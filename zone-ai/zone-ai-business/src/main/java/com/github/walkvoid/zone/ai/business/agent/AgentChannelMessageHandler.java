package com.github.walkvoid.zone.ai.business.agent;

import com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelReplySink;
import com.github.walkvoid.zone.ai.business.channel.weixin.WeiXinMediaDownloader;
import com.github.walkvoid.zone.ai.business.tool.AppLogSearchTool;
import com.github.walkvoid.zone.ai.business.tool.RepoChangeTool;
import com.github.walkvoid.zone.ai.business.tool.RepoReadTool;
import com.github.walkvoid.zone.ai.business.tool.SqlQueryTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
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

/**
 * 企微/飞书消息走大模型，挂上 BeeCloud 日志、只读 SQL 与代码沙箱读取。
 */
@Primary
@Component
public class AgentChannelMessageHandler implements ChannelMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentChannelMessageHandler.class);
    private static final String SYSTEM_PROMPT = """
            你是供应链金融排障助手，在企业微信群里回答。
            规则：
            1. 用户给 traceId、报错、某环境刚失败时，调用 beecloudSearchLogs；env 常见为 dev 或 qa。
            2. 查用户、合同、融资单、流水、资方等业务数据时，先 listNamedQueries，再 runNamedQuery。禁止手写 SQL。
            3. 问「代码在哪」「哪个类/方法」「这段逻辑怎么实现」时，先 listRepos 看沙箱，再 searchCode，需要细节时 readSourceFile。
            4. 用户明确要求改代码时：先 readSourceFile，再 describeWritePolicy 看 write-mode，然后直接 applyPatch 或 applyReplace。
               write-mode=DIFF_FILE 时 apply 只在源文件同级生成 .patch，不改源文件；DIRECT 才覆盖沙箱源文件。
            5. 用户发截图或图片时，先识别图中的文字、报错、traceId、单号，再按上面规则调用工具。
            6. 不知道就说不知道，禁止编造状态码、金额、接口路径、类名。
            7. 回复要短，适合群聊。工具原始 JSON 只提炼结论，不要整段贴回群。
            8. 同一群内的连续提问属于同一段对话，可沿用上文中的单号、traceId、结论。
            9. 记忆里只有用户短文本和你的短回复，没有工具原始 JSON；需要最新数据时再调工具。
            """;
    private static final int MAX_REPLY_CHARS = 3500;
    private static final long TIMEOUT_SECONDS = 120;
    private static final String RESET_REPLY = "已清空本群对话上下文，可以开始新问题。";

    private final ChatClient chatClient;
    private final GroupChatMemoryService groupChatMemoryService;
    private final AppLogSearchTool appLogSearchTool;
    private final SqlQueryTool sqlQueryTool;
    private final RepoReadTool repoReadTool;
    private final RepoChangeTool repoChangeTool;
    private final WeiXinMediaDownloader mediaDownloader;
    private final ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "weixin-agent");
        t.setDaemon(true);
        return t;
    });

    public AgentChannelMessageHandler(OpenAiChatModel chatModel,
                                      GroupChatMemoryService groupChatMemoryService,
                                      AppLogSearchTool appLogSearchTool,
                                      SqlQueryTool sqlQueryTool,
                                      RepoReadTool repoReadTool,
                                      RepoChangeTool repoChangeTool,
                                      WeiXinMediaDownloader mediaDownloader) {
        this.groupChatMemoryService = groupChatMemoryService;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(groupChatMemoryService.advisor())
                .build();
        this.appLogSearchTool = appLogSearchTool;
        this.sqlQueryTool = sqlQueryTool;
        this.repoReadTool = repoReadTool;
        this.repoChangeTool = repoChangeTool;
        this.mediaDownloader = mediaDownloader;
        log.info("Agent tools registered: beecloudSearchLogs, listNamedQueries/runNamedQuery, "
                + "listRepos/searchCode/readSourceFile, describeWritePolicy/applyPatch/applyReplace");
    }

    @Override
    public void onMessage(ChannelInboundMessage message, ChannelReplySink replySink) {
        String text = stripMention(message.getTextContent());
        boolean hasImages = message.hasImages();
        System.out.println("=======Agent inbound text=[" + text + "] images=" + message.getImages().size()
                + " user=" + message.getUserId() + "=======");
        log.info("[{}] agent inbound user={}, chat={}, conversationId={}, msgType={}, text={}, images={}",
                message.getChannelType(), message.getUserId(), message.getChatId(),
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
        log.info("agent askModel start, conversationId={}, tools=[log,sql,repo,change], text={}, images={}",
                conversationId, promptText, media.size());
        Media[] mediaArr = media.toArray(Media[]::new);
        return groupChatMemoryService.runExclusive(conversationId, () -> {
            var spec = chatClient.prompt()
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(u -> {
                        u.text(promptText);
                        if (mediaArr.length > 0) {
                            u.media(mediaArr);
                        }
                    })
                    .tools(appLogSearchTool, sqlQueryTool, repoReadTool, repoChangeTool);
            String content = spec.call().content();
            log.info("agent askModel done, conversationId={}, replyChars={}",
                    conversationId, content == null ? 0 : content.length());
            return StringUtils.hasText(content) ? content.trim() : "我这边没有生成到有效回答，请换个说法再问一次。";
        });
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
