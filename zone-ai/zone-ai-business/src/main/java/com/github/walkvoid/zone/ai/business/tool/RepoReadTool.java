package com.github.walkvoid.zone.ai.business.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoReadSupport;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoToolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 白名单仓库只读：列出仓库、搜索源码、按行读取。不写文件、不读密钥。
 */
@Component
public class RepoReadTool {

    private static final Logger log = LoggerFactory.getLogger(RepoReadTool.class);

    private final RepoReadSupport support;
    private final ObjectMapper mapper = new ObjectMapper();

    public RepoReadTool(RepoReadSupport support) {
        this.support = support;
    }

    @Tool(description = "列出允许读取的代码仓库及路径白名单。定位类或改代码之前先调用，确认沙箱是否就绪。")
    public JsonNode listRepos() {
        log.info("listRepos invoked, enabled={}, sandboxExists={}, root={}",
                support.properties().isEnabled(), support.sandboxExists(), support.properties().rootPath());
        RepoToolProperties properties = support.properties();
        ObjectNode repo = mapper.createObjectNode();
        repo.put("name", properties.displayName());
        repo.put("root", properties.rootPath().toString());
        repo.put("exists", support.sandboxExists());
        ArrayNode allow = mapper.createArrayNode();
        properties.normalizedAllowPaths().forEach(allow::add);
        repo.set("allowPaths", allow);
        repo.put("maxReadLines", properties.getMaxReadLines());
        repo.put("maxSearchResults", properties.getMaxSearchResults());

        ObjectNode result = mapper.createObjectNode();
        result.put("success", true);
        result.put("enabled", properties.isEnabled());
        ArrayNode repos = mapper.createArrayNode();
        repos.add(repo);
        result.set("repos", repos);
        if (!properties.isEnabled()) {
            result.put("hint", "Repo read tool is disabled (zone.ai.tool.repo.enabled=false).");
        } else if (!support.sandboxExists()) {
            result.put("hint", "Sandbox missing. Clone or git worktree zone to " + properties.rootPath());
        }
        log.info("listRepos done, exists={}, allowPaths={}", support.sandboxExists(), properties.normalizedAllowPaths());
        return result;
    }

    @Tool(description = "在沙箱白名单路径内搜索源码。keyword 匹配文件名或文件内容。"
            + "pathPrefix 可选，禁止密钥文件。")
    public JsonNode searchCode(
            @ToolParam(description = "搜索关键词，如类名、方法名、报错文案", required = true) String keyword,
            @ToolParam(description = "可选的相对路径前缀，限制搜索范围") String pathPrefix,
            @ToolParam(description = "最大命中条数，默认配置值，硬上限见 listRepos") Integer maxResults) {
        log.info("searchCode invoked, keyword={}, pathPrefix={}, maxResults={}", keyword, pathPrefix, maxResults);
        try {
            JsonNode notReady = notReady();
            if (notReady != null) {
                log.warn("searchCode skipped, not ready: {}", notReady.path("error").asText());
                return notReady;
            }
            int limit = maxResults == null ? support.properties().getMaxSearchResults() : maxResults;
            var hits = support.search(keyword, pathPrefix, limit);
            log.info("searchCode done, keyword={}, returned={}", keyword, hits.size());
            ArrayNode rows = mapper.createArrayNode();
            for (RepoReadSupport.SearchHit hit : hits) {
                ObjectNode row = mapper.createObjectNode();
                row.put("path", hit.path());
                row.put("line", hit.line());
                row.put("text", hit.text());
                rows.add(row);
            }
            ObjectNode result = mapper.createObjectNode();
            result.put("success", true);
            result.put("keyword", keyword);
            result.put("returned", hits.size());
            result.set("hits", rows);
            return result;
        } catch (Exception e) {
            log.warn("searchCode failed: {}", e.getMessage());
            return errorResult(e.getMessage());
        }
    }

    @Tool(description = "读取沙箱白名单内的源文件片段。path 相对沙箱根目录，例如 "
            + "jinkoscf-business-common/jinkoscf-business-common-service/src/main/java/.../Foo.java。"
            + "单次最多约 400 行。禁止 ..、绝对路径和密钥文件。")
    public JsonNode readSourceFile(
            @ToolParam(description = "相对沙箱根目录的文件路径", required = true) String path,
            @ToolParam(description = "起始行号，从 1 开始，默认 1") Integer startLine,
            @ToolParam(description = "结束行号（含），默认 startLine + 最大行数") Integer endLine) {
        log.info("readSourceFile invoked, path={}, startLine={}, endLine={}", path, startLine, endLine);
        try {
            JsonNode notReady = notReady();
            if (notReady != null) {
                log.warn("readSourceFile skipped, not ready: {}", notReady.path("error").asText());
                return notReady;
            }
            RepoReadSupport.FileSlice slice = support.read(path, startLine, endLine);
            log.info("readSourceFile done, path={}, lines={}~{}/{}",
                    slice.path(), slice.startLine(), slice.endLine(), slice.totalLines());
            ObjectNode result = mapper.createObjectNode();
            result.put("success", true);
            result.put("path", slice.path());
            result.put("startLine", slice.startLine());
            result.put("endLine", slice.endLine());
            result.put("totalLines", slice.totalLines());
            result.put("content", slice.content());
            return result;
        } catch (Exception e) {
            log.warn("readSourceFile failed: {}", e.getMessage());
            return errorResult(e.getMessage());
        }
    }

    private JsonNode notReady() {
        if (!support.properties().isEnabled()) {
            return errorResult("Repo read tool is disabled (zone.ai.tool.repo.enabled=false).");
        }
        if (!support.sandboxExists()) {
            return errorResult("Sandbox repo not found: " + support.properties().rootPath()
                    + ". Clone or git worktree the zone repo to this path first.");
        }
        return null;
    }

    private JsonNode errorResult(String msg) {
        ObjectNode err = mapper.createObjectNode();
        err.put("success", false);
        err.put("error", msg);
        return err;
    }
}
