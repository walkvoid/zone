package com.github.walkvoid.zone.ai.business.demo;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LangChain4j Demo: embedding model + Qdrant storage.
 *
 * <p>Core API flow: TextSegment -> EmbeddingModel.embedAll() -> EmbeddingStore.addAll()</p>
 *
 * @author jiangjunqing
 */
@Component
public class LangChain4jDemoService {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jDemoService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private QdrantEmbeddingStore embeddingStore;

    /**
     * Demo: embed a batch of Chinese text and store into Qdrant.
     */
    public void demoEmbedAndStore() {
        // 1. Build text segments (text + metadata)
        List<TextSegment> segments = buildDemoSegments();

        // 2. Call embedding model, batch generate vectors
        log.info("Calling embedding model, {} texts total", segments.size());
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        log.info("Embedding done, {} vectors, dimension={}",
                embeddings.size(),
                embeddings.isEmpty() ? 0 : embeddings.get(0).dimension());

        // 3. Batch write to Qdrant
        List<String> ids = embeddingStore.addAll(embeddings, segments);
        log.info("Stored into Qdrant, IDs={}", ids);
    }

    /**
     * Build demo text segments (supply chain finance domain knowledge).
     */
    private List<TextSegment> buildDemoSegments() {
        List<TextSegment> segments = new ArrayList<>();
        segments.add(TextSegment.from(
                "Supply chain finance is a comprehensive financial service where banks, "
                        + "centered around core enterprises, manage the capital and logistics flows "
                        + "of upstream and downstream SMEs, transforming uncontrollable risks of "
                        + "individual enterprises into controllable risks of the entire supply chain.",
                Metadata.from(Map.of("category", "definition", "source", "demo"))));
        segments.add(TextSegment.from(
                "Accounts receivable financing is one of the most common models in supply chain finance. "
                        + "Suppliers transfer their accounts receivable from core enterprises to "
                        + "financial institutions to obtain financing.",
                Metadata.from(Map.of("category", "financing_mode", "source", "demo"))));
        segments.add(TextSegment.from(
                "Inventory pledge financing refers to a financing method where enterprises use "
                        + "their own or third-party held inventory as collateral to apply for "
                        + "loans from financial institutions.",
                Metadata.from(Map.of("category", "financing_mode", "source", "demo"))));
        segments.add(TextSegment.from(
                "Prepayment financing is a business model where downstream distributors, "
                        + "based on genuine trade contracts with core enterprises, apply for "
                        + "financing from financial institutions to pay for goods.",
                Metadata.from(Map.of("category", "financing_mode", "source", "demo"))));
        segments.add(TextSegment.from(
                "Core enterprises play the role of credit intermediaries in supply chain finance. "
                        + "Their credit level directly determines the scale and cost of financing "
                        + "available to upstream and downstream enterprises.",
                Metadata.from(Map.of("category", "core_enterprise", "source", "demo"))));
        return segments;
    }
}