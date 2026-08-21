package com.github.walkvoid.zone.ai.knowledge;

import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownKnowledgeSplitterTest {

    @Test
    void splitsByMarkdownHeaders() {
        KnowledgeProperties properties = new KnowledgeProperties();
        properties.setMaxSegmentSize(500);
        properties.setMaxOverlapSize(50);
        MarkdownKnowledgeSplitter splitter = new MarkdownKnowledgeSplitter(properties);

        String md = """
                # 标题一
                这是第一段业务说明，描述资产签发概述。
                ## 子标题
                这是子章节内容，包含流程节点说明。
                # 标题二
                第二大段内容，描述审核与签署。
                """;

        List<TextSegment> segments = splitter.split(md, Map.of("source", "business/demo.md"));
        assertFalse(segments.isEmpty());
        assertTrue(segments.size() >= 2);

        List<org.springframework.ai.document.Document> docs =
                splitter.toSpringDocuments(segments, "business/demo.md", "business", "knowledge");
        assertFalse(docs.isEmpty());
        assertTrue(docs.get(0).getId().matches(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"));
        assertTrue(docs.get(0).getMetadata().get("doc_key").equals("knowledge:business/demo.md:0"));
        assertTrue(docs.get(0).getMetadata().get("kb").equals("knowledge"));
    }
}
