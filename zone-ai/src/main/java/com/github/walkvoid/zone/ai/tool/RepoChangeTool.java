package com.github.walkvoid.zone.ai.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.tool.repo.RepoToolProperties;
import com.github.walkvoid.zone.ai.tool.repo.RepoWriteMode;
import com.github.walkvoid.zone.ai.tool.repo.RepoWriteSupport;
import com.github.walkvoid.zone.ai.tool.repo.CodeChangeHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 沙箱内自动改代码：直接 apply 落盘。DIFF_FILE 写同级 .patch，DIRECT 覆盖源文件。
 * 不写密钥、不改 pom/配置文件。
 */
@Component
public class RepoChangeTool {

    private static final Logger log = LoggerFactory.getLogger(RepoChangeTool.class);

    private final RepoWriteSupport support;
    private final CodeChangeHistoryService history;
    public RepoChangeTool(RepoWriteSupport support) {
        this(support, (CodeChangeHistoryService) null);
    }

    @Autowired
    public RepoChangeTool(RepoWriteSupport support, ObjectProvider<CodeChangeHistoryService> history) {
        this(support, history == null ? null : history.getIfAvailable());
    }

    RepoChangeTool(RepoWriteSupport support, CodeChangeHistoryService history) {
        this.support = support;
        this.history = history;
    }

    @Tool(description = "查看代码写入策略：是否启用、write-mode（DIFF_FILE 只写同级 .patch / DIRECT 改源文件）、"
            + "可写路径白名单、单次 patch 行数/字节上限。改代码前应先 readSourceFile，再直接 applyPatch 或 applyReplace。")
    public JsonNode describeWritePolicy() {
        RepoToolProperties properties = support.properties();
        ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
        result.put("success", true);
        result.put("writeEnabled", properties.isWriteEnabled());
        result.put("writeMode", properties.getWriteMode().name());
        result.put("sandboxExists", support.sandboxExists());
        result.put("root", properties.rootPath().toString());
        result.put("maxPatchLines", properties.getMaxPatchLines());
        result.put("maxPatchBytes", properties.getMaxPatchBytes());
        ArrayNode allow = JsonUtils.getObjectMapper().createArrayNode();
        properties.normalizedWriteAllowPaths().forEach(allow::add);
        result.set("writeAllowPaths", allow);
        if (!properties.isEnabled()) {
            result.put("hint", "Repo tool is disabled.");
        } else if (!properties.isWriteEnabled()) {
            result.put("hint", "Write disabled. Set zone.ai.tool.repo.write-enabled=true.");
        } else if (!support.sandboxExists()) {
            result.put("hint", "Sandbox missing at " + properties.rootPath());
        } else if (properties.getWriteMode() == RepoWriteMode.DIFF_FILE) {
            result.put("hint", "Mode DIFF_FILE: applyPatch/applyReplace writes a sibling .patch, does not change source. "
                    + "Flow: readSourceFile → applyPatch/applyReplace.");
        } else {
            result.put("hint", "Mode DIRECT: applyPatch/applyReplace overwrites sandbox source. "
                    + "Flow: readSourceFile → applyPatch/applyReplace.");
        }
        return result;
    }

    @Tool(description = "按 write-mode 写入整文件修改。path 相对沙箱根目录；newContent 为修改后完整文件内容。"
            + "DIFF_FILE 在源文件同级写 .patch（不改源文件）；DIRECT 覆盖沙箱源文件。新建文件也走此接口。")
    public JsonNode applyPatch(
            @ToolParam(description = "相对沙箱根目录的文件路径", required = true) String path,
            @ToolParam(description = "修改后的完整文件内容", required = true) String newContent) {
        log.info("applyPatch invoked, path={}", path);
        try {
            RepoWriteSupport.ApplyResult applied = support.applyPatch(path, newContent);
            recordHistory(applied, "applyPatch");
            log.info("applyPatch done, path={}, newFile={}, written={}, sourceWritten={}, patchFile={}",
                    applied.path(), applied.newFile(), applied.written(), applied.sourceWritten(), applied.patchFile());
            return applyResult(applied);
        } catch (Exception e) {
            log.warn("applyPatch failed: {}", e.getMessage());
            return errorResult(e.getMessage());
        }
    }

    @Tool(description = "按 write-mode 做局部替换。oldText 必须在文件中存在；匹配多次且 replaceAll=false 会拒绝。"
            + "DIFF_FILE 只写同级 .patch；DIRECT 才改源文件。")
    public JsonNode applyReplace(
            @ToolParam(description = "相对沙箱根目录的文件路径", required = true) String path,
            @ToolParam(description = "要被替换的原文片段", required = true) String oldText,
            @ToolParam(description = "替换后的文本", required = true) String newText,
            @ToolParam(description = "是否替换所有匹配，默认 false（仅替换首个）") Boolean replaceAll) {
        log.info("applyReplace invoked, path={}, replaceAll={}", path, replaceAll);
        try {
            boolean all = Boolean.TRUE.equals(replaceAll);
            RepoWriteSupport.ApplyResult applied = support.applyReplace(path, oldText, newText, all);
            recordHistory(applied, "applyReplace");
            log.info("applyReplace done, path={}, written={}, sourceWritten={}, patchFile={}",
                    applied.path(), applied.written(), applied.sourceWritten(), applied.patchFile());
            return applyResult(applied);
        } catch (Exception e) {
            log.warn("applyReplace failed: {}", e.getMessage());
            return errorResult(e.getMessage());
        }
    }

    private void recordHistory(RepoWriteSupport.ApplyResult applied, String toolName) {
        if (history != null) {
            history.record(applied, toolName);
        }
    }

    private JsonNode applyResult(RepoWriteSupport.ApplyResult applied) {
        ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
        result.put("success", true);
        result.put("path", applied.path());
        result.put("newFile", applied.newFile());
        result.put("addedLines", applied.addedLines());
        result.put("removedLines", applied.removedLines());
        result.put("writeMode", applied.writeMode().name());
        result.put("written", applied.written());
        result.put("sourceWritten", applied.sourceWritten());
        if (applied.patchFile() != null) {
            result.put("patchFile", applied.patchFile());
        }
        return result;
    }

    private JsonNode errorResult(String msg) {
        ObjectNode err = JsonUtils.getObjectMapper().createObjectNode();
        err.put("success", false);
        err.put("error", msg);
        return err;
    }
}
