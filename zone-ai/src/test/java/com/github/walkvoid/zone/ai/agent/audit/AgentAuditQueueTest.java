package com.github.walkvoid.zone.ai.agent.audit;

import com.github.walkvoid.zone.ai.agent.AgentAuditProperties;
import com.github.walkvoid.zone.ai.agent.AgentTurnContext;
import com.github.walkvoid.zone.ai.agent.CodeChangeTurnContext;
import com.github.walkvoid.zone.ai.db.dao.AiAgentStepDAO;
import com.github.walkvoid.zone.ai.db.dao.AiAgentTurnDAO;
import com.github.walkvoid.zone.ai.tool.repo.CodeChangeHistoryService;
import com.github.walkvoid.zone.ai.db.entity.AiAgentTurn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentAuditQueueTest {

    private AgentAuditQueue queue;

    @AfterEach
    void tearDown() {
        if (queue != null) {
            queue.shutdown();
        }
    }

    @Test
    void asyncFalsePersistsInline() {
        AgentAuditProperties properties = new AgentAuditProperties();
        properties.setAsync(false);
        AiAgentTurnDAO turnDAO = mock(AiAgentTurnDAO.class);
        when(turnDAO.selectByTurnNo("t1")).thenReturn(null);
        queue = newQueue(properties, turnDAO, mock(AiAgentStepDAO.class));

        boolean offered = queue.offer(AgentAuditEvent.turnStart(turn("t1"), false));

        assertTrue(offered);
        verify(turnDAO).insert(any(AiAgentTurn.class));
    }

    @Test
    void dropsWhenQueueFull() throws Exception {
        AgentAuditProperties properties = new AgentAuditProperties();
        properties.setAsync(true);
        properties.setQueueCapacity(64);
        properties.setShutdownDrainMs(500);
        AiAgentTurnDAO turnDAO = mock(AiAgentTurnDAO.class);
        when(turnDAO.selectByTurnNo(any())).thenReturn(null);
        CountDownLatch blocked = new CountDownLatch(1);
        when(turnDAO.insert(any())).thenAnswer(invocation -> {
            blocked.await(15, TimeUnit.SECONDS);
            return 1;
        });
        queue = newQueue(properties, turnDAO, mock(AiAgentStepDAO.class));

        int dropped = 0;
        try {
            for (int i = 0; i < 80; i++) {
                if (!queue.offer(AgentAuditEvent.turnStart(turn("t-" + i), false))) {
                    dropped++;
                }
            }
        } finally {
            blocked.countDown();
        }

        assertTrue(dropped > 0, "full queue should drop events");
    }

    @Test
    void finishRecountsToolCalls() {
        AgentAuditProperties properties = new AgentAuditProperties();
        properties.setAsync(false);
        AiAgentTurnDAO turnDAO = mock(AiAgentTurnDAO.class);
        AiAgentStepDAO stepDAO = mock(AiAgentStepDAO.class);
        AiAgentTurn existing = new AiAgentTurn();
        existing.setTurnNo("t1");
        when(turnDAO.selectByTurnNo("t1")).thenReturn(existing);
        when(stepDAO.countByTurnNo("t1")).thenReturn(2);
        queue = newQueue(properties, turnDAO, stepDAO);

        queue.offer(AgentAuditEvent.turnFinish(
                turn("t1"), AgentTurnContext.STATUS_SUCCESS, "ok", null, 12L, false));

        verify(turnDAO).finish("t1", AgentTurnContext.STATUS_SUCCESS, "ok", null, 12L, 2);
    }

    @Test
    void disabledDoesNotPersist() {
        AgentAuditProperties properties = new AgentAuditProperties();
        properties.setEnabled(false);
        properties.setAsync(false);
        AiAgentTurnDAO turnDAO = mock(AiAgentTurnDAO.class);
        queue = newQueue(properties, turnDAO, mock(AiAgentStepDAO.class));
        assertFalse(queue.isEnabled());
        assertFalse(queue.offer(AgentAuditEvent.turnStart(turn("t1"), false)));
        verify(turnDAO, org.mockito.Mockito.never()).insert(any());
    }

    private AgentAuditQueue newQueue(AgentAuditProperties properties,
                                     AiAgentTurnDAO turnDAO,
                                     AiAgentStepDAO stepDAO) {
        @SuppressWarnings("unchecked")
        ObjectProvider<CodeChangeHistoryService> history = mock(ObjectProvider.class);
        when(history.getIfAvailable()).thenReturn(null);
        return new AgentAuditQueue(properties, turnDAO, stepDAO, history);
    }

    private static CodeChangeTurnContext.Turn turn(String turnNo) {
        return new CodeChangeTurnContext.Turn(
                "weixin:bot:group", "weixin:bot:group:s1", turnNo, "msg", "bot", "demo",
                "chat", "user", "WEIXIN", "hello");
    }
}
