package com.github.walkvoid.zone.ai.prompt;

import com.github.walkvoid.wvframework.fileservice.FileService;
import com.github.walkvoid.wvframework.fileservice.entity.FileInfo;
import com.github.walkvoid.zone.ai.knowledge.MarkdownKnowledgeSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Prompt 运行材料：多文件抽取 → 切割入向量 → 按 file_ids 检索 → 可批量清理。
 */
@Service
@EnableConfigurationProperties(PromptRunMaterialProperties.class)
public class PromptRunMaterialService {

    private static final Logger log = LoggerFactory.getLogger(PromptRunMaterialService.class);

    private final PromptRunMaterialProperties properties;
    private final FileService fileService;
    private final DocumentTextExtractor extractor;
    private final MarkdownKnowledgeSplitter splitter;
    private final VectorStore vectorStore;

    public PromptRunMaterialService(PromptRunMaterialProperties properties,
                                    FileService fileService,
                                    DocumentTextExtractor extractor,
                                    MarkdownKnowledgeSplitter splitter,
                                    VectorStore vectorStore) {
        this.properties = properties;
        this.fileService = fileService;
        this.extractor = extractor;
        this.splitter = splitter;
        this.vectorStore = vectorStore;
    }

    /**
     * 多文件准备注入文本。任一 docx/超长则走向量；短文本直接拼接。
     */
    public PreparedMaterial prepareMany(List<Long> fileIds, String query, boolean forceVector) {
        List<Long> ids = normalizeIds(fileIds);
        if (ids.isEmpty()) {
            return new PreparedMaterial("", false, 0, List.of());
        }
        if (ids.size() > properties.getMaxFiles()) {
            throw new IllegalArgumentException("附件最多 " + properties.getMaxFiles() + " 个，当前 " + ids.size());
        }

        List<String> inlineParts = new ArrayList<>();
        List<Long> vectorIds = new ArrayList<>();
        int chunkCount = 0;

        for (Long fileId : ids) {
            FileInfo info = fileService.getById(fileId);
            if (info == null) {
                throw new IllegalArgumentException("文件不存在: " + fileId);
            }
            String fullText = extractor.extract(fileId);
            String title = info.getOriginalName() == null ? String.valueOf(fileId) : info.getOriginalName();
            boolean useVector = forceVector
                    || DocumentTextExtractor.isDocx(info)
                    || fullText.length() > properties.getFullTextMaxChars();
            if (useVector) {
                chunkCount += ingest(fileId, info, fullText);
                vectorIds.add(fileId);
            } else {
                inlineParts.add("## " + title + "\n\n" + fullText);
            }
        }

        StringBuilder document = new StringBuilder();
        if (!vectorIds.isEmpty()) {
            String retrievalQuery = StringUtils.hasText(query) ? query : "文档要点";
            List<Document> hits = searchMany(vectorIds, retrievalQuery, properties.getRagTopK());
            if (hits.isEmpty()) {
                document.append("[向量检索无命中，请调整 task/question]\n");
            } else {
                document.append(hits.stream().map(doc -> {
                    Object name = doc.getMetadata() == null ? null : doc.getMetadata().get("filename");
                    String head = name == null ? "" : ("### " + name + "\n");
                    return head + doc.getText();
                }).collect(Collectors.joining("\n\n---\n\n")));
            }
        }
        if (!inlineParts.isEmpty()) {
            if (!document.isEmpty()) {
                document.append("\n\n");
            }
            document.append(String.join("\n\n---\n\n", inlineParts));
        }
        return new PreparedMaterial(document.toString(), !vectorIds.isEmpty(), chunkCount, ids);
    }

    public int ingest(Long fileId, FileInfo info, String fullText) {
        deleteByFileId(fileId);
        Map<String, Object> base = new LinkedHashMap<>();
        base.put("file_id", String.valueOf(fileId));
        base.put("filename", info.getOriginalName());
        List<TextSegment> segments = splitter.split(fullText, base);
        List<Document> docs = toDocuments(fileId, info, segments);
        if (!docs.isEmpty()) {
            int batchSize = 64;
            for (int i = 0; i < docs.size(); i += batchSize) {
                vectorStore.add(new ArrayList<>(docs.subList(i, Math.min(i + batchSize, docs.size()))));
            }
        }
        log.info("prompt-run material ingested fileId={}, chunks={}", fileId, docs.size());
        return docs.size();
    }

    public List<Document> searchMany(List<Long> fileIds, String query, int topK) {
        List<String> idStrs = normalizeIds(fileIds).stream().map(String::valueOf).toList();
        if (idStrs.isEmpty()) {
            return List.of();
        }
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Object> idObjs = new ArrayList<>(idStrs);
        Filter.Expression filter = b.and(
                b.eq("kb", properties.getKbTag()),
                b.in("file_id", idObjs)
        ).build();
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(Math.max(1, Math.min(topK, 20)))
                .similarityThreshold(properties.getSimilarityThreshold())
                .filterExpression(filter)
                .build();
        List<Document> hits = vectorStore.similaritySearch(request);
        return hits == null ? List.of() : hits;
    }

    public void deleteByFileId(Long fileId) {
        if (fileId == null) {
            return;
        }
        deleteByFileIds(List.of(fileId));
    }

    public void deleteByFileIds(List<Long> fileIds) {
        List<Long> ids = normalizeIds(fileIds);
        if (ids.isEmpty()) {
            return;
        }
        try {
            List<String> idStrs = ids.stream().map(String::valueOf).toList();
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            List<Object> idObjs = new ArrayList<>(idStrs);
            Filter.Expression expr = b.and(
                    b.eq("kb", properties.getKbTag()),
                    b.in("file_id", idObjs)
            ).build();
            vectorStore.delete(expr);
            log.info("prompt-run material vectors deleted fileIds={}", ids);
        } catch (Exception e) {
            log.warn("delete prompt-run vectors failed fileIds={}: {}", ids, e.getMessage());
        }
    }

    private List<Document> toDocuments(Long fileId, FileInfo info, List<TextSegment> segments) {
        List<Document> result = new ArrayList<>();
        String filename = info.getOriginalName() == null ? String.valueOf(fileId) : info.getOriginalName();
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            if (!StringUtils.hasText(segment.text())) {
                continue;
            }
            Map<String, Object> meta = new LinkedHashMap<>();
            if (segment.metadata() != null) {
                meta.putAll(segment.metadata().toMap());
            }
            meta.put("kb", properties.getKbTag());
            meta.put("file_id", String.valueOf(fileId));
            meta.put("filename", filename);
            meta.put("chunk_index", i);
            String docKey = "prompt_run:" + fileId + ":" + i;
            meta.put("doc_key", docKey);
            String id = UUID.nameUUIDFromBytes(docKey.getBytes(StandardCharsets.UTF_8)).toString();
            result.add(new Document(id, segment.text().trim(), meta));
        }
        return result;
    }

    static List<Long> normalizeIds(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        return fileIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    public record PreparedMaterial(String documentText, boolean vectorMode, int chunkCount, List<Long> fileIds) {
    }
}
