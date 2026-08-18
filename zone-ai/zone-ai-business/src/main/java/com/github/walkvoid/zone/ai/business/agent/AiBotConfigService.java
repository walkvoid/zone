package com.github.walkvoid.zone.ai.business.agent;

import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.zone.ai.business.channel.core.ChannelType;
import com.github.walkvoid.zone.ai.business.channel.weixin.WeiXinAiBotProperties;
import com.github.walkvoid.zone.ai.business.db.dao.AiBotConfigDAO;
import com.github.walkvoid.zone.ai.model.entity.AiBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 从 {@code ai_bot_config} 读取机器人；表为空时回退到 properties 里的单个企微 bot。
 */
@Service
public class AiBotConfigService {

    private static final Logger log = LoggerFactory.getLogger(AiBotConfigService.class);

    static final String FALLBACK_SYSTEM_PROMPT = """
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

    static final String DEFAULT_TOOL_CODES = "log,sql,repo_read,repo_change";

    private final AiBotConfigDAO dao;

    public AiBotConfigService(AiBotConfigDAO dao) {
        this.dao = dao;
    }

    public List<AiBotConfig> listEnabledWeixin(WeiXinAiBotProperties properties) {
        List<AiBotConfig> fromDb = safeList(ChannelType.WEIXIN.name());
        Map<String, AiBotConfig> unique = new LinkedHashMap<>();
        for (AiBotConfig bot : fromDb) {
            if (bot == null || !StringUtils.hasText(bot.getBotId()) || !StringUtils.hasText(bot.getSecret())) {
                log.warn("skip ai_bot_config id={}, missing botId or secret", bot == null ? null : bot.getId());
                continue;
            }
            unique.putIfAbsent(bot.getBotId().trim(), bot);
        }
        if (!unique.isEmpty()) {
            return new ArrayList<>(unique.values());
        }
        if (properties != null && properties.hasCredentials()) {
            log.warn("ai_bot_config has no enabled WEIXIN bot, fallback to zone.ai.channel.weixin.bot-id");
            return List.of(fallbackFromProperties(properties));
        }
        return List.of();
    }

    public AiBotConfig findByBotId(String botId, WeiXinAiBotProperties properties) {
        AiBotConfig fromDb = null;
        if (StringUtils.hasText(botId)) {
            try {
                fromDb = dao.selectByBotId(botId);
            } catch (Exception e) {
                log.warn("load ai_bot_config failed, botId={}: {}", botId, e.getMessage());
            }
        }
        if (fromDb != null && fromDb.getIsEnabled() != BooleanEnum.NO) {
            return fromDb;
        }
        if (properties != null && properties.hasCredentials()
                && StringUtils.hasText(botId)
                && botId.trim().equals(properties.getBotId().trim())) {
            return fallbackFromProperties(properties);
        }
        return null;
    }

    public static AiBotConfig fallbackFromProperties(WeiXinAiBotProperties properties) {
        AiBotConfig bot = new AiBotConfig();
        bot.setBotCode("properties-fallback");
        bot.setBotId(properties.getBotId().trim());
        bot.setBotName("Zone AI");
        bot.setSecret(properties.getSecret());
        bot.setChannelType(ChannelType.WEIXIN.name());
        bot.setSystemPrompt(FALLBACK_SYSTEM_PROMPT);
        bot.setToolCodes(DEFAULT_TOOL_CODES);
        bot.setWelcomeText(properties.getWelcomeText());
        bot.setIsEnabled(BooleanEnum.YES);
        return bot;
    }

    public static String resolvePrompt(AiBotConfig config) {
        if (config != null && StringUtils.hasText(config.getSystemPrompt())) {
            return config.getSystemPrompt().trim();
        }
        return FALLBACK_SYSTEM_PROMPT;
    }

    public static List<AgentToolCode> resolveTools(AiBotConfig config) {
        return AgentToolCode.parse(config == null ? null : config.getToolCodes());
    }

    private List<AiBotConfig> safeList(String channelType) {
        try {
            return dao.selectEnabledByChannel(channelType);
        } catch (Exception e) {
            log.warn("list ai_bot_config failed: {}", e.getMessage());
            return List.of();
        }
    }
}
