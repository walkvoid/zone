package com.github.walkvoid.zone.ai.business.agent.audit;

import com.github.walkvoid.zone.ai.business.agent.AgentAuditProperties;
import com.github.walkvoid.zone.ai.business.agent.AgentTurnContext;
import com.github.walkvoid.zone.ai.business.agent.CodeChangeTurnContext;
import com.github.walkvoid.zone.ai.business.db.dao.AiAgentStepDAO;
import com.github.walkvoid.zone.ai.business.db.dao.AiAgentTurnDAO;
import com.github.walkvoid.zone.ai.business.tool.repo.CodeChangeHistoryService;
import com.github.walkvoid.zone.ai.model.entity.AiAgentStep;
import com.github.walkvoid.zone.ai.model.entity.AiAgentTurn;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 进程内有界审计队列。对话线程只 {@code offer}，单线程消费者写库。
 */
@Component
public class AgentAuditQueue {

    private static final Logger log = LoggerFactory.getLogger(AgentAuditQueue.class);
    private static final int ANSWER_MAX = 4000;

    private final AgentAuditProperties properties;
    private final AiAgentTurnDAO turnDAO;
    private final AiAgentStepDAO stepDAO;
    private final ObjectProvider<CodeChangeHistoryService> history;
    private final BlockingQueue<AgentAuditEvent> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread worker;

    public AgentAuditQueue(AgentAuditProperties properties,
                           AiAgentTurnDAO turnDAO,
                           AiAgentStepDAO stepDAO,
                           ObjectProvider<CodeChangeHistoryService> history) {
        this.properties = properties;
        this.turnDAO = turnDAO;
        this.stepDAO = stepDAO;
        this.history = history;
        this.queue = new ArrayBlockingQueue<>(properties.normalizedQueueCapacity());
        this.worker = new Thread(this::loop, "agent-audit-writer");
        this.worker.setDaemon(true);
        if (properties.isEnabled() && properties.isAsync()) {
            this.worker.start();
            log.info("agent audit queue ready, capacity={}, maxJsonBytes={}",
                    properties.normalizedQueueCapacity(), properties.normalizedMaxJsonBytes());
        } else {
            log.info("agent audit queue inline, enabled={}, async={}",
                    properties.isEnabled(), properties.isAsync());
        }
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public boolean offer(AgentAuditEvent event) {
        if (!properties.isEnabled() || event == null) {
            return false;
        }
        if (!properties.isAsync()) {
            persistQuietly(event);
            return true;
        }
        if (queue.offer(event)) {
            return true;
        }
        String turnNo = event.turn() == null ? null : event.turn().turnNo();
        log.warn("agent audit queue full, drop type={} turnNo={} tool={}",
                event.type(), turnNo, event.toolName());
        return false;
    }

    int size() {
        return queue.size();
    }

    private void loop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                AgentAuditEvent event = queue.poll(200, TimeUnit.MILLISECONDS);
                if (event != null) {
                    persistQuietly(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        worker.interrupt();
        int wait = properties.normalizedShutdownDrainMs();
        if (wait <= 0) {
            return;
        }
        try {
            worker.join(wait);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<AgentAuditEvent> rest = new ArrayList<>();
        queue.drainTo(rest);
        for (AgentAuditEvent event : rest) {
            persistQuietly(event);
        }
        if (!rest.isEmpty()) {
            log.info("agent audit drained {} events on shutdown", rest.size());
        }
    }

    private void persistQuietly(AgentAuditEvent event) {
        try {
            persist(event);
        } catch (Exception e) {
            String turnNo = event.turn() == null ? null : event.turn().turnNo();
            log.warn("agent audit persist failed type={} turnNo={}: {}",
                    event.type(), turnNo, e.getMessage());
        }
    }

    void persist(AgentAuditEvent event) {
        switch (event.type()) {
            case TURN_START -> insertTurn(event);
            case TOOL -> insertStep(event);
            case CODE_CHANGE -> persistCodeChange(event);
            case TURN_FINISH -> finishTurn(event);
        }
    }

    private void insertTurn(AgentAuditEvent event) {
        CodeChangeTurnContext.Turn turn = event.turn();
        if (turn == null || turnDAO.selectByTurnNo(turn.turnNo()) != null) {
            return;
        }
        AiAgentTurn row = new AiAgentTurn();
        row.setConversationId(turn.conversationId());
        row.setSessionId(turn.sessionId());
        row.setTurnNo(turn.turnNo());
        row.setMessageId(turn.messageId());
        row.setBotId(turn.botId());
        row.setBotCode(turn.botCode());
        row.setChatId(turn.chatId());
        row.setUserId(turn.userId());
        row.setChannelType(turn.channelType());
        row.setUserText(AgentAuditJson.truncate(turn.requestText(), properties.normalizedMaxJsonBytes()));
        row.setHasImage(event.hasImage() ? 1 : 0);
        row.setStatus(AgentTurnContext.STATUS_RUNNING);
        row.setToolCallCount(0);
        row.setCreateTime(LocalDateTime.now());
        turnDAO.insert(row);
    }

    private void insertStep(AgentAuditEvent event) {
        CodeChangeTurnContext.Turn turn = event.turn();
        if (turn == null) {
            return;
        }
        AiAgentStep step = new AiAgentStep();
        step.setTurnNo(turn.turnNo());
        step.setSeq(event.seq());
        step.setStepType("TOOL");
        step.setToolCode(event.toolCode());
        step.setToolName(event.toolName());
        step.setRequestJson(event.requestJson());
        step.setResponseJson(event.responseJson());
        step.setResponseSummary(event.responseSummary());
        step.setSuccess(event.success() ? 1 : 0);
        step.setDurationMs(event.durationMs());
        step.setErrorMessage(event.errorMessage());
        step.setCreateTime(LocalDateTime.now());
        stepDAO.insert(step);
        turnDAO.incrementToolCallCount(turn.turnNo());
    }

    private void persistCodeChange(AgentAuditEvent event) {
        CodeChangeHistoryService service = history.getIfAvailable();
        if (service == null || event.turn() == null || event.applied() == null) {
            return;
        }
        service.persist(event.turn(), event.applied(), event.toolName());
    }

    private void finishTurn(AgentAuditEvent event) {
        CodeChangeTurnContext.Turn turn = event.turn();
        if (turn == null) {
            return;
        }
        if (turnDAO.selectByTurnNo(turn.turnNo()) == null) {
            insertTurn(event);
        }
        turnDAO.finish(
                turn.turnNo(),
                event.status(),
                AgentAuditJson.truncate(event.finalAnswer(), ANSWER_MAX),
                AgentAuditJson.truncate(event.errorMessage(), 512),
                event.durationMs(),
                stepDAO.countByTurnNo(turn.turnNo()));
    }
}
