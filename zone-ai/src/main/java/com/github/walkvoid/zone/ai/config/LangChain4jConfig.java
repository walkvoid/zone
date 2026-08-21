//package com.github.walkvoid.zone.ai.config;
//
//import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * LangChain4j manual configuration (Qdrant has no official Spring Boot Starter,
// * requires explicit Bean declaration).
// *
// * @author jiangjunqing
// */
//@Configuration
//public class LangChain4jConfig {
//
//    @Value("${langchain4j.qdrant.host:172.16.205.4}")
//    private String host;
//
//    @Value("${langchain4j.qdrant.port:6334}")
//    private int port;
//
//    @Value("${langchain4j.qdrant.api-key:}")
//    private String apiKey;
//
//    @Value("${langchain4j.qdrant.collection-name:supply_finance_rag}")
//    private String collectionName;
//
//    @Value("${langchain4j.qdrant.use-tls:false}")
//    private boolean useTls;
//
//    @Value("${langchain4j.qdrant.payload-text-key:doc_content}")
//    private String payloadTextKey;
//
//    @Bean
//    public QdrantEmbeddingStore qdrantEmbeddingStore() {
//        return new QdrantEmbeddingStore(collectionName, host, port, useTls, payloadTextKey, apiKey);
//    }
//}