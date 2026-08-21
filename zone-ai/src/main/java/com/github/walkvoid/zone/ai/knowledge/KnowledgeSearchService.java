package com.github.walkvoid.zone.ai.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI VectorStore 语义检索（knowledge 文档）。
 */
@Service
public class KnowledgeSearchService {

    private final KnowledgeProperties properties;
    private final VectorStore vectorStore;

    public KnowledgeSearchService(KnowledgeProperties properties, VectorStore vectorStore) {
        this.properties = properties;
        this.vectorStore = vectorStore;
    }

    public List<Document> search(String query, String category, Integer topK) {
        if (!properties.isEnabled() || !StringUtils.hasText(query)) {
            return List.of();
        }
        int k = topK == null || topK <= 0 ? properties.getDefaultTopK() : Math.min(topK, 20);

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filter;
        if (StringUtils.hasText(category)) {
            filter = b.and(
                    b.eq("kb", properties.getKbTag()),
                    b.eq("category", category.trim())
            ).build();
        } else {
            filter = b.eq("kb", properties.getKbTag()).build();
        }

        SearchRequest request = SearchRequest.builder()
                .query(query.trim())
                .topK(k)
                .similarityThreshold(properties.getSimilarityThreshold())
                .filterExpression(filter)
                .build();
        List<Document> hits = vectorStore.similaritySearch(request);
        return hits == null ? List.of() : new ArrayList<>(hits);
    }
}
