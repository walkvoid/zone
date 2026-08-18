package com.github.walkvoid.zone.ai.business.tool.repo;

import com.github.walkvoid.zone.ai.business.agent.CodeChangeTurnContext;
import com.github.walkvoid.zone.ai.business.agent.audit.AgentAuditEvent;
import com.github.walkvoid.zone.ai.business.agent.audit.AgentAuditQueue;
import com.github.walkvoid.zone.ai.business.db.dao.AiCodeChangeDAO;
import com.github.walkvoid.zone.ai.business.db.dao.AiCodeChangePatchDAO;
import com.github.walkvoid.zone.ai.model.entity.AiCodeChange;
import com.github.walkvoid.zone.ai.model.entity.AiCodeChangePatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CodeChangeHistoryServiceTest {

    @TempDir
    Path temp;

    private Path javaFile;
    private AiCodeChangeDAO changeDAO;
    private AiCodeChangePatchDAO patchDAO;
    private CodeChangeHistoryService service;
    private final List<AiCodeChange> storedChanges = new ArrayList<>();
    private final List<AiCodeChangePatch> storedPatches = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        javaFile = temp.resolve("zone-finance/src/PayListener.java");
        Files.createDirectories(javaFile.getParent());
        Files.writeString(javaFile, """
                package demo;
                public class PayListener {
                    public void onSuccess() {
                        // 放款成功写回
                    }
                }
                """);
        changeDAO = mock(AiCodeChangeDAO.class);
        patchDAO = mock(AiCodeChangePatchDAO.class);
        RepoToolProperties properties = new RepoToolProperties();
        properties.setRoot(temp.toString());
        properties.setAllowPaths(List.of("zone-finance/**"));
        properties.setWriteEnabled(true);
        properties.setWriteAllowPaths(List.of("zone-finance/**"));
        properties.setWriteMode(RepoWriteMode.DIFF_FILE);
        service = new CodeChangeHistoryService(changeDAO, patchDAO, new RepoWriteSupport(properties));

        when(changeDAO.insert(any())).thenAnswer(invocation -> {
            AiCodeChange entity = invocation.getArgument(0);
            entity.setId(10L);
            storedChanges.add(entity);
            return 1;
        });
        when(changeDAO.selectByTurnNo("turn-1")).thenAnswer(invocation ->
                storedChanges.stream().filter(c -> "turn-1".equals(c.getTurnNo())).findFirst().orElse(null));
        when(changeDAO.selectById(10L)).thenAnswer(invocation ->
                storedChanges.stream().filter(c -> Long.valueOf(10L).equals(c.getId())).findFirst().orElse(null));
        when(changeDAO.updateById(any())).thenAnswer(invocation -> {
            AiCodeChange update = invocation.getArgument(0);
            storedChanges.stream()
                    .filter(c -> c.getId().equals(update.getId()))
                    .findFirst()
                    .ifPresent(existing -> {
                        if (update.getPatchCount() != null) {
                            existing.setPatchCount(update.getPatchCount());
                        }
                        if (update.getStatus() != null) {
                            existing.setStatus(update.getStatus());
                        }
                        if (update.getApplyMessage() != null) {
                            existing.setApplyMessage(update.getApplyMessage());
                        }
                    });
            return 1;
        });
        when(patchDAO.insert(any())).thenAnswer(invocation -> {
            AiCodeChangePatch patch = invocation.getArgument(0);
            patch.setId((long) (storedPatches.size() + 1));
            storedPatches.add(patch);
            return 1;
        });
        when(patchDAO.selectByChangeId(10L)).thenAnswer(invocation -> List.copyOf(storedPatches));
        when(patchDAO.updateById(any())).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        CodeChangeTurnContext.close();
    }

    @Test
    void titleUsesRequestText() {
        assertEquals("把文案改一下", CodeChangeHistoryService.titleOf("把文案改一下", "a.java"));
        assertEquals("修改 a.java", CodeChangeHistoryService.titleOf("  ", "a.java"));
    }

    @Test
    void recordEnqueuesWhenAuditQueueEnabled() {
        AgentAuditQueue auditQueue = mock(AgentAuditQueue.class);
        when(auditQueue.isEnabled()).thenReturn(true);
        when(auditQueue.offer(any())).thenReturn(true);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentAuditQueue> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(auditQueue);
        service = new CodeChangeHistoryService(changeDAO, patchDAO, new RepoWriteSupport(serviceWriteProps()), provider);
        CodeChangeTurnContext.open(new CodeChangeTurnContext.Turn(
                "weixin:bot:group", "weixin:bot:group:s1", "turn-1", "msg-1", "bot", "supply-chain",
                "group", "zhangsan", "WEIXIN", "修一下文案"));
        RepoWriteSupport.ApplyResult applied = new RepoWriteSupport.ApplyResult(
                "zone-finance/src/PayListener.java", false, 1, 1, true, false,
                RepoWriteMode.DIFF_FILE, "a.patch", "diff", "old", "new");

        service.record(applied, "applyPatch");

        verify(auditQueue).offer(any(AgentAuditEvent.class));
        verify(changeDAO, times(0)).insert(any());
    }

    @Test
    void recordSkippedWithoutTurnContext() {
        RepoWriteSupport.ApplyResult applied = new RepoWriteSupport.ApplyResult(
                "zone-finance/src/PayListener.java", false, 1, 1, true, false,
                RepoWriteMode.DIFF_FILE, "a.patch", "diff", "old", "new");
        service.record(applied, "applyPatch");
        verify(changeDAO, times(0)).insert(any());
    }

    @Test
    void twoPatchesInSameTurnShareOneChange() throws Exception {
        CodeChangeTurnContext.open(new CodeChangeTurnContext.Turn(
                "weixin:bot:group", "weixin:bot:group:s1", "turn-1", "msg-1", "bot", "supply-chain",
                "group", "zhangsan", "WEIXIN", "修一下文案和注释"));
        RepoWriteSupport support = new RepoWriteSupport(serviceWriteProps());
        RepoWriteSupport.ApplyResult first = support.applyReplace(
                "zone-finance/src/PayListener.java", "放款成功写回", "放款成功写回（A）", false);
        RepoWriteSupport.ApplyResult second = support.applyReplace(
                "zone-finance/src/PayListener.java", "package demo;", "package demo; // x", false);
        service.record(first, "applyReplace");
        service.record(second, "applyReplace");

        assertEquals(1, storedChanges.size());
        assertEquals("weixin:bot:group", storedChanges.get(0).getConversationId());
        assertEquals("turn-1", storedChanges.get(0).getTurnNo());
        assertEquals(2, storedPatches.size());
        assertEquals(Integer.valueOf(2), storedChanges.get(0).getPatchCount());
        assertEquals(CodeChangeHistoryService.STATUS_PENDING, storedChanges.get(0).getStatus());
    }

    @Test
    void applyWritesSourceWhenUnchanged() throws Exception {
        String original = Files.readString(javaFile);
        CodeChangeTurnContext.open(new CodeChangeTurnContext.Turn(
                "weixin:bot:group", "weixin:bot:group:s1", "turn-1", null, "bot", "supply-chain",
                "group", "zhangsan", "WEIXIN", "改文案"));
        RepoWriteSupport support = new RepoWriteSupport(serviceWriteProps());
        RepoWriteSupport.ApplyResult applied = support.applyReplace(
                "zone-finance/src/PayListener.java", "放款成功写回", "放款成功写回（已改）", false);
        service.record(applied, "applyReplace");
        assertEquals(original, Files.readString(javaFile));

        CodeChangeHistoryService.ApplyOutcome outcome = service.apply(10L);
        assertEquals(CodeChangeHistoryService.STATUS_APPLIED, outcome.status());
        assertTrue(Files.readString(javaFile).contains("放款成功写回（已改）"));
    }

    @Test
    void applyConflictsWhenSourceChanged() throws Exception {
        CodeChangeTurnContext.open(new CodeChangeTurnContext.Turn(
                "weixin:bot:group", "weixin:bot:group:s1", "turn-1", null, "bot", "supply-chain",
                "group", "zhangsan", "WEIXIN", "改文案"));
        RepoWriteSupport support = new RepoWriteSupport(serviceWriteProps());
        RepoWriteSupport.ApplyResult applied = support.applyReplace(
                "zone-finance/src/PayListener.java", "放款成功写回", "放款成功写回（已改）", false);
        service.record(applied, "applyReplace");
        Files.writeString(javaFile, Files.readString(javaFile) + "\n// changed by human\n");

        CodeChangeHistoryService.ApplyOutcome outcome = service.apply(10L);
        assertEquals(CodeChangeHistoryService.STATUS_FAILED, outcome.status());
        ArgumentCaptor<AiCodeChangePatch> captor = ArgumentCaptor.forClass(AiCodeChangePatch.class);
        verify(patchDAO).updateById(captor.capture());
        assertEquals(CodeChangeHistoryService.STATUS_CONFLICT, captor.getValue().getStatus());
    }

    @Test
    void applyRejectedForDirectMode() {
        AiCodeChange change = new AiCodeChange();
        change.setId(10L);
        change.setWriteMode("DIRECT");
        change.setStatus(CodeChangeHistoryService.STATUS_APPLIED);
        storedChanges.add(change);
        assertThrows(IllegalStateException.class, () -> service.apply(10L));
    }

    private RepoToolProperties serviceWriteProps() {
        RepoToolProperties properties = new RepoToolProperties();
        properties.setRoot(temp.toString());
        properties.setAllowPaths(List.of("zone-finance/**"));
        properties.setWriteEnabled(true);
        properties.setWriteAllowPaths(List.of("zone-finance/**"));
        properties.setWriteMode(RepoWriteMode.DIFF_FILE);
        return properties;
    }
}
