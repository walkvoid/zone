package com.github.walkvoid.zone.ai.db.vec;

import com.github.walkvoid.zone.ai.knowledge.KnowledgeIngestService;
import com.github.walkvoid.zone.ai.model.enums.FinancingStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Qdrant RAG knowledge base DAO.
 * Stores domain knowledge (e.g. financing status descriptions) into the vector database
 * for later semantic retrieval by LLM agents.
 *
 * @author jiangjunqing
 * @date 2026/8/6
 */
@Repository
public class QdrantRagDAO {

    private static final Logger log = LoggerFactory.getLogger(QdrantRagDAO.class);

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private KnowledgeIngestService knowledgeIngestService;

    /**
     * 全量灌库：清空旧 knowledge 文档后，把 classpath knowledge 下全部 Markdown 切割写入 Qdrant。
     */
    public KnowledgeIngestService.IngestResult rebuildKnowledgeBase() {
        KnowledgeIngestService.IngestResult result = knowledgeIngestService.rebuildAll();
        log.info("rebuild knowledge base done enabled={}, files={}, chunks={}, failed={}",
                result.enabled(), result.fileCount(), result.chunkCount(), result.failedFiles().size());
        return result;
    }

    /**
     * Store all financing status enum values into the Qdrant vector database.
     * Each status description (Chinese text) is embedded as a vector, with metadata
     * containing the enum name, code, and ordinal for downstream filtering.
     */
    public void storeFinancingStatuses() {
        List<Document> documents = new ArrayList<>();
        for (FinancingStatusEnum status : FinancingStatusEnum.values()) {
            Map<String, Object> metadata = Map.of(
                    "name", status.name(),
                    "code", status.getCode(),
                    "ordinal", status.ordinal()
            );
            documents.add(new Document(status.getDesc(), metadata));
        }
        vectorStore.add(documents);
        log.info("Stored {} financing statuses into Qdrant", documents.size());
    }
}
