package com.github.walkvoid.zone.ai.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.walkvoid.wvframework.utils.JsonUtils;
import com.github.walkvoid.zone.ai.knowledge.KnowledgeProperties;
import com.github.walkvoid.zone.ai.knowledge.KnowledgeSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 供应链业务/服务/代码地图知识库检索（Qdrant）。
 */
@Component
public class KnowledgeSearchTool {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeSearchTool.class);

    private final KnowledgeSearchService searchService;
    private final KnowledgeProperties properties;

    public KnowledgeSearchTool(KnowledgeSearchService searchService, KnowledgeProperties properties) {
        this.searchService = searchService;
        this.properties = properties;
    }

    @Tool(description = "检索供应链金融知识库（业务文档、服务说明、代码地图）。"
            + "用户问流程怎么走、某模块职责、代码地图入口时优先调用。"
            + "category 可选：business / service / codemap；不传则全库检索。")
    public JsonNode searchKnowledge(
            @ToolParam(description = "检索问题或关键词，用中文自然语言描述", required = true) String query,
            @ToolParam(description = "可选分类：business、service、codemap") String category,
            @ToolParam(description = "返回条数，默认 5，最大 20") Integer topK) {
        log.info("searchKnowledge invoked, query={}, category={}, topK={}", query, category, topK);
        ObjectNode result = JsonUtils.getObjectMapper().createObjectNode();
        if (!properties.isEnabled()) {
            result.put("success", false);
            result.put("hint", "Knowledge search disabled (zone.ai.knowledge.enabled=false).");
            return result;
        }
        List<Document> hits = searchService.search(query, category, topK);
        result.put("success", true);
        result.put("count", hits.size());
        ArrayNode rows = JsonUtils.getObjectMapper().createArrayNode();
        for (Document doc : hits) {
            ObjectNode row = JsonUtils.getObjectMapper().createObjectNode();
            row.put("text", doc.getText());
            if (doc.getScore() != null) {
                row.put("score", doc.getScore());
            }
            Map<String, Object> meta = doc.getMetadata();
            if (meta != null) {
                if (meta.get("source") != null) {
                    row.put("source", String.valueOf(meta.get("source")));
                }
                if (meta.get("category") != null) {
                    row.put("category", String.valueOf(meta.get("category")));
                }
                if (meta.get("filename") != null) {
                    row.put("filename", String.valueOf(meta.get("filename")));
                }
            }
            rows.add(row);
        }
        result.set("hits", rows);
        if (hits.isEmpty()) {
            result.put("hint", "无命中。可先确认已调用 POST /ai/knowledge/ingest 灌库，或换关键词/分类。");
        }
        log.info("searchKnowledge done, hits={}", hits.size());
        return result;
    }
}
