package com.github.walkvoid.zone.ai.knowledge;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LangChain4j 切割：先按 Markdown 标题拆成章节，再对超长章节 {@link DocumentSplitters#recursive}。
 */
@Component
public class MarkdownKnowledgeSplitter {

    /** 在 H1~H6 标题前切开（lookahead，保留标题文本） */
    static final String MARKDOWN_HEADER_REGEX = "(?m)(?=^#{1,6}\\s)";

    private final KnowledgeProperties properties;

    public MarkdownKnowledgeSplitter(KnowledgeProperties properties) {
        this.properties = properties;
    }

    public List<TextSegment> split(String markdown, Map<String, Object> baseMetadata) {
        if (!StringUtils.hasText(markdown)) {
            return List.of();
        }
        int maxSize = Math.max(200, properties.getMaxSegmentSize());
        int overlap = Math.max(0, Math.min(properties.getMaxOverlapSize(), maxSize / 2));
        DocumentSplitter recursive = DocumentSplitters.recursive(maxSize, overlap);
        Metadata metadata = Metadata.from(toStringMap(baseMetadata));

        List<TextSegment> result = new ArrayList<>();
        for (String section : markdown.split(MARKDOWN_HEADER_REGEX)) {
            String trimmed = section == null ? "" : section.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }
            Document sectionDoc = Document.from(trimmed, metadata.copy());
            result.addAll(recursive.split(sectionDoc));
        }
        return result;
    }

    /**
     * 转为 Spring AI Document 列表（带稳定 id，便于覆盖写入）。
     */
    public List<org.springframework.ai.document.Document> toSpringDocuments(
            List<TextSegment> segments,
            String source,
            String category,
            String kbTag) {
        List<org.springframework.ai.document.Document> result = new ArrayList<>(segments.size());
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            String text = segment.text();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            Map<String, Object> meta = new LinkedHashMap<>();
            if (segment.metadata() != null) {
                meta.putAll(segment.metadata().toMap());
            }
            meta.put("kb", kbTag);
            meta.put("source", source);
            meta.put("category", category);
            meta.put("chunk_index", i);
            String docKey = "knowledge:" + source + ":" + i;
            meta.put("doc_key", docKey);
            String title = firstHeading(text);
            if (StringUtils.hasText(title)) {
                meta.put("title", title);
            }
            // QdrantVectorStore 要求 point id 为合法 UUID
            String id = UUID.nameUUIDFromBytes(docKey.getBytes(StandardCharsets.UTF_8)).toString();
            result.add(new org.springframework.ai.document.Document(id, text.trim(), meta));
        }
        return result;
    }

    static String firstHeading(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        for (String line : text.split("\\R", 8)) {
            String t = line.trim();
            if (t.startsWith("#")) {
                return t.replaceFirst("^#+\\s*", "").trim();
            }
        }
        return null;
    }

    private static Map<String, Object> toStringMap(Map<String, Object> base) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (base == null) {
            return map;
        }
        for (Map.Entry<String, Object> e : base.entrySet()) {
            if (e.getKey() != null && e.getValue() != null) {
                map.put(e.getKey(), e.getValue());
            }
        }
        return map;
    }
}
