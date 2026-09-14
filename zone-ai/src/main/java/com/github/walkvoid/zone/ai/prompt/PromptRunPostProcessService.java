package com.github.walkvoid.zone.ai.prompt;

import com.github.walkvoid.zone.ai.agent.AgentToolCode;
import com.github.walkvoid.zone.ai.agent.AgentToolRegistry;
import com.github.walkvoid.zone.ai.agent.AgentTurnContext;
import com.github.walkvoid.zone.ai.agent.CodeChangeTurnContext;
import com.github.walkvoid.zone.ai.agent.audit.AgentAuditEvent;
import com.github.walkvoid.zone.ai.agent.audit.AgentAuditQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Prompt 运行结果后处理：按用户选择的单个系统工具 + 自然语言指令，调用 ChatClient 执行。
 */
@Service
public class PromptRunPostProcessService {

    private static final Logger log = LoggerFactory.getLogger(PromptRunPostProcessService.class);

    private static final String SYSTEM_PROMPT = """
            你是 Prompt 运行结果的后处理助手。
            用户会给出一条处理指令，以及一段 Prompt 模板的运行结果。
            你必须优先调用已挂载的工具完成指令；不要编造工具已执行成功。
            完成后用简洁中文说明做了什么、工具返回的关键信息。
            """;

    private final ChatClient chatClient;
    private final AgentToolRegistry agentToolRegistry;
    private final AgentAuditQueue auditQueue;

    public PromptRunPostProcessService(ChatClient.Builder chatClientBuilder,
                                       AgentToolRegistry agentToolRegistry,
                                       ObjectProvider<AgentAuditQueue> auditQueue) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        this.agentToolRegistry = agentToolRegistry;
        this.auditQueue = auditQueue == null ? null : auditQueue.getIfAvailable();
    }

    /**
     * @param toolCode    {@link AgentToolCode#code()}
     * @param instruction 用户自然语言指令，如「请将结果文件上传到 minio」
     * @param runResult   Prompt 运行结果正文
     */
    public String process(String toolCode, String instruction, String runResult) {
        AgentToolCode code = AgentToolCode.fromCode(toolCode);
        if (code == null) {
            throw new IllegalArgumentException("未知工具编码: " + toolCode);
        }
        if (!code.ready() || !agentToolRegistry.isRegistered(code)) {
            throw new IllegalArgumentException(
                    "工具「" + code.label() + "」尚未实现，请稍后再试或选择其它工具");
        }
        if (!StringUtils.hasText(instruction)) {
            throw new IllegalArgumentException("请填写处理指令");
        }
        if (!StringUtils.hasText(runResult)) {
            throw new IllegalArgumentException("运行结果为空，无法处理");
        }

        Object[] tools = agentToolRegistry.resolve(List.of(code));
        if (tools.length == 0) {
            throw new IllegalArgumentException("工具「" + code.label() + "」未注册");
        }

        String truncated = truncate(runResult.trim(), 60_000);
        String userMessage = """
                【处理指令】
                %s

                【Prompt 运行结果】
                %s
                """.formatted(instruction.trim(), truncated);

        String turnNo = UUID.randomUUID().toString().replace("-", "");
        CodeChangeTurnContext.Turn turn = new CodeChangeTurnContext.Turn(
                "prompt-post-process",
                "prompt-post-process",
                turnNo,
                null,
                null,
                "prompt-post-process",
                null,
                null,
                "PROMPT_POST_PROCESS",
                instruction.trim());

        log.info("prompt run post-process start, tool={}, instructionChars={}, resultChars={}",
                code.code(), instruction.length(), truncated.length());

        AgentTurnContext.open(turn, false);
        offerAudit(AgentAuditEvent.turnStart(turn, false));
        try {
            var spec = chatClient.prompt()
                    .user(userMessage)
                    .tools(tools);
            String content = spec.call().content();
            String answer = StringUtils.hasText(content) ? content.trim() : "（模型未返回有效内容）";
            log.info("prompt run post-process done, tool={}, replyChars={}", code.code(), answer.length());
            offerAudit(AgentAuditEvent.turnFinish(
                    turn,
                    AgentTurnContext.STATUS_SUCCESS,
                    answer,
                    null,
                    AgentTurnContext.currentState() == null ? 0L : AgentTurnContext.currentState().elapsedMs(),
                    false));
            return answer;
        } catch (RuntimeException e) {
            offerAudit(AgentAuditEvent.turnFinish(
                    turn,
                    AgentTurnContext.STATUS_FAILED,
                    null,
                    e.getMessage(),
                    AgentTurnContext.currentState() == null ? 0L : AgentTurnContext.currentState().elapsedMs(),
                    false));
            throw e;
        } finally {
            AgentTurnContext.close();
        }
    }

    private void offerAudit(AgentAuditEvent event) {
        if (auditQueue != null) {
            auditQueue.offer(event);
        }
    }

    private static String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "\n\n...[结果已截断，共 " + text.length() + " 字符]";
    }
}
