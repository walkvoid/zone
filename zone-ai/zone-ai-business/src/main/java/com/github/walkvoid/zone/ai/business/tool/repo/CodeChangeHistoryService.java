package com.github.walkvoid.zone.ai.business.tool.repo;

import com.github.walkvoid.zone.ai.business.agent.CodeChangeTurnContext;
import com.github.walkvoid.zone.ai.business.agent.audit.AgentAuditEvent;
import com.github.walkvoid.zone.ai.business.agent.audit.AgentAuditQueue;
import com.github.walkvoid.zone.ai.business.db.dao.AiCodeChangeDAO;
import com.github.walkvoid.zone.ai.business.db.dao.AiCodeChangePatchDAO;
import com.github.walkvoid.zone.ai.model.entity.AiCodeChange;
import com.github.walkvoid.zone.ai.model.entity.AiCodeChangePatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 记录 AI 改代码历史，并在 DIFF_FILE 模式下把 patch 应用到沙箱源文件。
 */
@Service
public class CodeChangeHistoryService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPLIED = 1;
    public static final int STATUS_PARTIAL = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_CONFLICT = 2;

    private static final int TITLE_MAX = 80;
    private static final int REQUEST_MAX = 1000;
    private static final Logger log = LoggerFactory.getLogger(CodeChangeHistoryService.class);

    private final AiCodeChangeDAO changeDAO;
    private final AiCodeChangePatchDAO patchDAO;
    private final RepoWriteSupport writeSupport;
    private final ObjectProvider<AgentAuditQueue> auditQueue;

    public CodeChangeHistoryService(AiCodeChangeDAO changeDAO,
                                    AiCodeChangePatchDAO patchDAO,
                                    RepoWriteSupport writeSupport) {
        this(changeDAO, patchDAO, writeSupport, null);
    }

    @Autowired
    public CodeChangeHistoryService(AiCodeChangeDAO changeDAO,
                                    AiCodeChangePatchDAO patchDAO,
                                    RepoWriteSupport writeSupport,
                                    ObjectProvider<AgentAuditQueue> auditQueue) {
        this.changeDAO = changeDAO;
        this.patchDAO = patchDAO;
        this.writeSupport = writeSupport;
        this.auditQueue = auditQueue;
    }

    /**
     * 对话热路径：入队后立即返回。无队列（单测）时同步落库。
     */
    public void record(RepoWriteSupport.ApplyResult applied, String toolName) {
        CodeChangeTurnContext.Turn turn = CodeChangeTurnContext.current();
        if (turn == null || applied == null || !applied.written()) {
            return;
        }
        AgentAuditQueue queue = auditQueue == null ? null : auditQueue.getIfAvailable();
        if (queue != null && queue.isEnabled()) {
            queue.offer(AgentAuditEvent.codeChange(turn, applied, toolName));
            return;
        }
        persist(turn, applied, toolName);
    }

    /** 审计队列消费者调用；页面 Apply 仍走 {@link #apply(Long)}。 */
    public void persist(CodeChangeTurnContext.Turn turn,
                        RepoWriteSupport.ApplyResult applied,
                        String toolName) {
        try {
            save(turn, applied, toolName);
        } catch (Exception e) {
            log.warn("save code change history failed, path={}: {}",
                    applied == null ? null : applied.path(), e.getMessage());
        }
    }

    public ApplyOutcome apply(Long changeId) {
        AiCodeChange change = changeDAO.selectById(changeId);
        if (change == null) {
            throw new IllegalArgumentException("改动记录不存在");
        }
        if (!RepoWriteMode.DIFF_FILE.name().equals(change.getWriteMode())) {
            throw new IllegalStateException("仅 DIFF_FILE 模式可以在页面上应用；DIRECT 已写入源文件");
        }
        if (Objects.equals(change.getStatus(), STATUS_APPLIED)) {
            return new ApplyOutcome(STATUS_APPLIED, "已经全部应用过", List.of());
        }
        List<AiCodeChangePatch> patches = patchDAO.selectByChangeId(changeId);
        if (patches.isEmpty()) {
            throw new IllegalStateException("没有可应用的 patch");
        }
        List<String> messages = new ArrayList<>();
        int appliedCount = 0;
        int conflictCount = 0;
        int failedCount = 0;
        for (AiCodeChangePatch patch : patches) {
            if (Objects.equals(patch.getStatus(), STATUS_APPLIED)) {
                appliedCount++;
                continue;
            }
            PatchApplyResult one = applyOne(patch);
            patch.setStatus(one.status());
            patch.setErrorMessage(one.message());
            patch.setApplyTime(LocalDateTime.now());
            patchDAO.updateById(patch);
            messages.add(patch.getSourcePath() + ": " + one.message());
            if (one.status() == STATUS_APPLIED) {
                appliedCount++;
            } else if (one.status() == STATUS_CONFLICT) {
                conflictCount++;
            } else {
                failedCount++;
            }
        }
        int status;
        String summary;
        if (failedCount == 0 && conflictCount == 0) {
            status = STATUS_APPLIED;
            summary = "已应用 " + appliedCount + " 个文件";
        } else if (appliedCount > 0) {
            status = STATUS_PARTIAL;
            summary = "部分成功：应用 " + appliedCount + "，冲突 " + conflictCount + "，失败 " + failedCount;
        } else {
            status = STATUS_FAILED;
            summary = "未应用成功：冲突 " + conflictCount + "，失败 " + failedCount;
        }
        change.setStatus(status);
        change.setApplyMessage(summary);
        change.setApplyTime(LocalDateTime.now());
        change.setUpdateTime(LocalDateTime.now());
        changeDAO.updateById(change);
        return new ApplyOutcome(status, summary, messages);
    }

    private synchronized void save(CodeChangeTurnContext.Turn turn,
                                   RepoWriteSupport.ApplyResult applied,
                                   String toolName) {
        AiCodeChange parent = changeDAO.selectByTurnNo(turn.turnNo());
        LocalDateTime now = LocalDateTime.now();
        boolean direct = applied.writeMode() == RepoWriteMode.DIRECT;
        int status = direct ? STATUS_APPLIED : STATUS_PENDING;
        if (parent == null) {
            parent = new AiCodeChange();
            parent.setConversationId(turn.conversationId());
            parent.setTurnNo(turn.turnNo());
            parent.setMessageId(turn.messageId());
            parent.setBotId(turn.botId());
            parent.setBotCode(turn.botCode());
            parent.setChatId(turn.chatId());
            parent.setUserId(turn.userId());
            parent.setChannelType(turn.channelType());
            parent.setTitle(titleOf(turn.requestText(), applied.path()));
            parent.setRequestText(truncate(turn.requestText(), REQUEST_MAX));
            parent.setWriteMode(applied.writeMode().name());
            parent.setStatus(status);
            parent.setPatchCount(0);
            parent.setCreateTime(now);
            parent.setUpdateTime(now);
            if (direct) {
                parent.setApplyTime(now);
                parent.setApplyMessage("DIRECT 模式已写入源文件");
            }
            changeDAO.insert(parent);
        }
        AiCodeChangePatch patch = new AiCodeChangePatch();
        patch.setChangeId(parent.getId());
        patch.setSourcePath(applied.path());
        patch.setPatchFile(applied.patchFile());
        patch.setToolName(toolName);
        patch.setNewFile(applied.newFile() ? 1 : 0);
        patch.setAddedLines(applied.addedLines());
        patch.setRemovedLines(applied.removedLines());
        patch.setUnifiedDiff(applied.unifiedDiff());
        patch.setBaseContent(applied.baseContent());
        patch.setNewContent(applied.newContent());
        patch.setStatus(status);
        patch.setCreateTime(now);
        if (direct) {
            patch.setApplyTime(now);
        }
        patchDAO.insert(patch);

        AiCodeChange update = new AiCodeChange();
        update.setId(parent.getId());
        update.setPatchCount((parent.getPatchCount() == null ? 0 : parent.getPatchCount()) + 1);
        update.setUpdateTime(now);
        if (!direct && Objects.equals(parent.getStatus(), STATUS_APPLIED)) {
            update.setStatus(STATUS_PENDING);
        }
        changeDAO.updateById(update);
    }

    private PatchApplyResult applyOne(AiCodeChangePatch patch) {
        if (patch.getNewContent() == null) {
            return new PatchApplyResult(STATUS_FAILED, "缺少目标文件内容");
        }
        try {
            String current = writeSupport.readSourceOrEmpty(patch.getSourcePath());
            boolean exists = StringUtils.hasText(current) || writeSupport.sourceExists(patch.getSourcePath());
            boolean newFile = Objects.equals(patch.getNewFile(), 1);
            if (newFile && exists && !Objects.equals(current, patch.getBaseContent())) {
                return new PatchApplyResult(STATUS_CONFLICT, "目标文件已存在且内容与生成时不一致");
            }
            if (!newFile) {
                String base = patch.getBaseContent() == null ? "" : patch.getBaseContent();
                if (!Objects.equals(current, base)) {
                    return new PatchApplyResult(STATUS_CONFLICT, "源文件已变化，拒绝覆盖");
                }
            }
            writeSupport.writeSource(patch.getSourcePath(), patch.getNewContent());
            return new PatchApplyResult(STATUS_APPLIED, "已写入源文件");
        } catch (Exception e) {
            log.warn("apply patch {} failed: {}", patch.getSourcePath(), e.getMessage());
            return new PatchApplyResult(STATUS_FAILED, e.getMessage());
        }
    }

    static String titleOf(String requestText, String path) {
        if (StringUtils.hasText(requestText)) {
            return truncate(requestText.replace('\n', ' ').trim(), TITLE_MAX);
        }
        if (StringUtils.hasText(path)) {
            return "修改 " + path;
        }
        return "代码修改";
    }

    private static String truncate(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.length() <= max) {
            return trimmed;
        }
        return trimmed.substring(0, max);
    }

    public record ApplyOutcome(int status, String message, List<String> details) {
    }

    private record PatchApplyResult(int status, String message) {
    }
}
