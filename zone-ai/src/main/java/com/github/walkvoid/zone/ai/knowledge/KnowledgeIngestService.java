package com.github.walkvoid.zone.ai.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描 classpath knowledge Markdown → LangChain4j 切割 → Spring AI VectorStore 写入 Qdrant。
 */
@Service
public class KnowledgeIngestService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestService.class);

    private final KnowledgeProperties properties;
    private final MarkdownKnowledgeSplitter splitter;
    private final VectorStore vectorStore;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public KnowledgeIngestService(KnowledgeProperties properties,
                                  MarkdownKnowledgeSplitter splitter,
                                  VectorStore vectorStore) {
        this.properties = properties;
        this.splitter = splitter;
        this.vectorStore = vectorStore;
    }

    /**
     * 全量灌库：先按 metadata.kb 删除旧向量，再扫描 classpath knowledge 下全部 Markdown，切割并写入 Qdrant。
     */
    public IngestResult rebuildAll() {
        return ingestAll(true);
    }

    /**
     * 灌入 classpath knowledge Markdown。
     *
     * @param replaceExisting true 时先删除本模块（kb 标记）已有向量再写入
     */
    public IngestResult ingestAll(boolean replaceExisting) {
        if (!properties.isEnabled()) {
            return IngestResult.disabled();
        }
        if (replaceExisting) {
            deleteKnowledgePoints();
        }

        Resource[] resources;
        try {
            resources = resolver.getResources(properties.getLocationPattern());
        } catch (IOException e) {
            throw new IllegalStateException("扫描 knowledge 失败: " + properties.getLocationPattern(), e);
        }

        int fileCount = 0;
        int chunkCount = 0;
        List<String> failed = new ArrayList<>();
        List<Document> batch = new ArrayList<>();

        for (Resource resource : resources) {
            if (resource == null || !resource.isReadable()) {
                continue;
            }
            String source = resolveSource(resource);
            if (!StringUtils.hasText(source) || !source.endsWith(".md")) {
                continue;
            }
            try {
                List<Document> docs = splitResource(resource, source);
                batch.addAll(docs);
                fileCount++;
                chunkCount += docs.size();
                log.info("knowledge split ok source={}, chunks={}", source, docs.size());
            } catch (Exception e) {
                failed.add(source + ": " + e.getMessage());
                log.warn("knowledge ingest failed source={}: {}", source, e.getMessage());
            }
        }

        addInBatches(batch);

        log.info("knowledge ingest done files={}, chunks={}, failed={}", fileCount, chunkCount, failed.size());
        return new IngestResult(true, fileCount, chunkCount, failed);
    }

    /**
     * 按 source 删除该文档全部向量（不重建其它文件）。
     * 例：{@code codemap/建档审核代码地图.md}
     */
    public int deleteBySource(String source) {
        if (!properties.isEnabled()) {
            return 0;
        }
        String normalized = normalizeSource(source);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("source 不能为空");
        }
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expr = b.and(
                b.eq("kb", properties.getKbTag()),
                b.eq("source", normalized)
        ).build();
        vectorStore.delete(expr);
        log.info("deleted knowledge vectors source={}", normalized);
        return 1;
    }

    /**
     * 单文件重灌：先按 source 删除旧 chunk，再重新切割写入。
     */
    public IngestResult reingestSource(String source) {
        if (!properties.isEnabled()) {
            return IngestResult.disabled();
        }
        String normalized = normalizeSource(source);
        if (!StringUtils.hasText(normalized) || !normalized.endsWith(".md")) {
            throw new IllegalArgumentException("source 须为 knowledge 下 md 相对路径，如 codemap/建档审核代码地图.md");
        }

        deleteBySource(normalized);

        Resource[] resources;
        try {
            resources = resolver.getResources(properties.getLocationPattern());
        } catch (IOException e) {
            throw new IllegalStateException("扫描 knowledge 失败: " + properties.getLocationPattern(), e);
        }

        for (Resource resource : resources) {
            if (resource == null || !resource.isReadable()) {
                continue;
            }
            String resolved = resolveSource(resource);
            if (!normalized.equals(resolved)) {
                continue;
            }
            try {
                List<Document> docs = splitResource(resource, resolved);
                addInBatches(docs);
                log.info("knowledge reingest ok source={}, chunks={}", resolved, docs.size());
                return new IngestResult(true, 1, docs.size(), List.of());
            } catch (Exception e) {
                log.warn("knowledge reingest failed source={}: {}", resolved, e.getMessage());
                return new IngestResult(true, 0, 0, List.of(resolved + ": " + e.getMessage()));
            }
        }
        return new IngestResult(true, 0, 0, List.of(normalized + ": classpath 中未找到该文件"));
    }

    private List<Document> splitResource(Resource resource, String source) throws IOException {
        String markdown = resource.getContentAsString(StandardCharsets.UTF_8);
        String category = resolveCategory(source);
        Map<String, Object> baseMeta = new LinkedHashMap<>();
        baseMeta.put("source", source);
        baseMeta.put("category", category);
        baseMeta.put("filename", filenameOf(source));
        var segments = splitter.split(markdown, baseMeta);
        return splitter.toSpringDocuments(segments, source, category, properties.getKbTag());
    }

    private void addInBatches(List<Document> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        int batchSize = 64;
        for (int i = 0; i < batch.size(); i += batchSize) {
            List<Document> part = batch.subList(i, Math.min(i + batchSize, batch.size()));
            vectorStore.add(new ArrayList<>(part));
        }
    }

    private void deleteKnowledgePoints() {
        try {
            Filter.Expression expr = new FilterExpressionBuilder()
                    .eq("kb", properties.getKbTag())
                    .build();
            vectorStore.delete(expr);
            log.info("deleted existing knowledge vectors kb={}", properties.getKbTag());
        } catch (Exception e) {
            log.warn("delete knowledge vectors failed (will still add): {}", e.getMessage());
        }
    }

    static String normalizeSource(String source) {
        if (!StringUtils.hasText(source)) {
            return "";
        }
        String s = source.trim().replace('\\', '/');
        while (s.startsWith("/")) {
            s = s.substring(1);
        }
        int idx = s.indexOf("knowledge/");
        if (idx >= 0) {
            s = s.substring(idx + "knowledge/".length());
        }
        return s;
    }

    static String resolveSource(Resource resource) {
        try {
            String url = resource.getURL().toString().replace('\\', '/');
            int idx = url.indexOf("/knowledge/");
            if (idx >= 0) {
                return url.substring(idx + "/knowledge/".length());
            }
            return resource.getFilename();
        } catch (IOException e) {
            return resource.getFilename();
        }
    }

    static String resolveCategory(String source) {
        if (!StringUtils.hasText(source)) {
            return "unknown";
        }
        int slash = source.indexOf('/');
        if (slash > 0) {
            return source.substring(0, slash);
        }
        return "root";
    }

    private static String filenameOf(String source) {
        int slash = source.lastIndexOf('/');
        return slash >= 0 ? source.substring(slash + 1) : source;
    }

    public record IngestResult(boolean enabled, int fileCount, int chunkCount, List<String> failedFiles) {
        static IngestResult disabled() {
            return new IngestResult(false, 0, 0, List.of());
        }
    }
}
