package com.github.walkvoid.zone.ai.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.zone.ai.knowledge.KnowledgeIngestService;
import com.github.walkvoid.zone.ai.knowledge.KnowledgeSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库灌库与检索（管理用）。
 */
@Tag(name = "Knowledge")
@RestController
@RequestMapping("/ai/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeIngestService ingestService;

    @Autowired
    private KnowledgeSearchService searchService;

    @Operation(summary = "全量灌库：清空旧 knowledge 向量后，把 classpath:knowledge/**/*.md 写入 Qdrant")
    @PostMapping("/rebuild")
    public ApiResult<Map<String, Object>> rebuild() {
        return toResult(ingestService.rebuildAll());
    }

    @Operation(summary = "灌入 knowledge Markdown；replace=true 时先删旧再写")
    @PostMapping("/ingest")
    public ApiResult<Map<String, Object>> ingest(
            @RequestParam(value = "replace", defaultValue = "true") boolean replace) {
        return toResult(ingestService.ingestAll(replace));
    }

    @Operation(summary = "按 source 删除该文档全部向量（不碰其它文件）")
    @DeleteMapping("/by-source")
    public ApiResult<Map<String, Object>> deleteBySource(@RequestParam("source") String source) {
        ingestService.deleteBySource(source);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deleted", true);
        body.put("source", source);
        return ApiResult.ok(body);
    }

    @Operation(summary = "单文件重灌：按 source 删旧 chunk 后重新切割写入")
    @PostMapping("/reingest")
    public ApiResult<Map<String, Object>> reingest(@RequestParam("source") String source) {
        return toResult(ingestService.reingestSource(source));
    }

    private static ApiResult<Map<String, Object>> toResult(KnowledgeIngestService.IngestResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", result.enabled());
        body.put("fileCount", result.fileCount());
        body.put("chunkCount", result.chunkCount());
        body.put("failedFiles", result.failedFiles());
        return ApiResult.ok(body);
    }

    @Operation(summary = "语义检索知识库（调试）")
    @PostMapping("/search")
    public ApiResult<List<Map<String, Object>>> search(@RequestBody Map<String, Object> request) {
        String query = request.get("query") == null ? null : String.valueOf(request.get("query"));
        String category = request.get("category") == null ? null : String.valueOf(request.get("category"));
        Integer topK = null;
        if (request.get("topK") instanceof Number n) {
            topK = n.intValue();
        }
        List<Document> hits = searchService.search(query, category, topK);
        List<Map<String, Object>> rows = hits.stream().map(doc -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", doc.getId());
            row.put("text", doc.getText());
            row.put("score", doc.getScore());
            row.put("metadata", doc.getMetadata());
            return row;
        }).toList();
        return ApiResult.ok(rows);
    }
}
