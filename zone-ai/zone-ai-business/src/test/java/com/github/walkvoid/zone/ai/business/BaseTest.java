package com.github.walkvoid.zone.ai.business;

import com.github.walkvoid.zone.ai.business.db.mapper.AiModelMapper;
import com.github.walkvoid.zone.ai.business.db.mapper.McpServerConfigMapper;
import com.github.walkvoid.zone.ai.business.db.mapper.PromptTemplateMapper;
import com.github.walkvoid.zone.ai.business.db.mapper.PromptTemplateRunRecordMapper;
import com.github.walkvoid.zone.ai.business.db.vec.QdrantRagDAO;
import com.github.walkvoid.zone.ai.business.tool.AppLogSearchTool;
import com.github.walkvoid.zone.ai.business.tool.SqlQueryTool;
import com.github.walkvoid.zone.ai.business.tool.sql.SqlQuerySupport;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author jiangjunqing
 * @date 2026/8/6
 */
@SpringBootTest(classes = AiApplication.class)
@ActiveProfiles({"lls", "test"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseTest {

    @MockBean
    private AiModelMapper aiModelMapper;

    @MockBean
    private McpServerConfigMapper mcpServerConfigMapper;

    @MockBean
    private PromptTemplateMapper promptTemplateMapper;

    @MockBean
    private PromptTemplateRunRecordMapper promptTemplateRunRecordMapper;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private QdrantRagDAO qdrantRagDAO;

    @Autowired
    private VectorStore vectorStore;

    @Test
    @Order(1)
    void testPrivateEmbeddingConnect() {
        String text = "测试私有embedding连通";
        float[] vec = embeddingModel.embed(text);

        System.out.println("向量长度 = " + vec.length);
        assertEquals(1536, vec.length);
    }

    @Test
    @Order(2)
    void testStoreFinancingStatuses() {
        qdrantRagDAO.storeFinancingStatuses();

        List<Document> results = vectorStore.similaritySearch("融资状态");
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find financing status documents in Qdrant");

        System.out.println("Found " + results.size() + " documents for query '融资状态'");
        results.forEach(doc -> System.out.println(
                "  text=" + doc.getText()
                + " | name=" + doc.getMetadata().get("name")
                + " | code=" + doc.getMetadata().get("code")));
    }

    @Test
    @Order(3)
    void testQueryFinancingSuccessStatus() {
        qdrantRagDAO.storeFinancingStatuses();

        List<Document> results = vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .query("融资交易已经成功状态是什么")
                        .topK(24)
                        .build());

        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find matching status for financing success query");

        System.out.println("=== 融资成功状态检索结果 (Top " + results.size() + ") ===");
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            System.out.printf("  #%d text=%-10s | name=%-20s | code=%-20s | score=%.4f%n",
                    i + 1, doc.getText(), doc.getMetadata().get("name"),
                    doc.getMetadata().get("code"), doc.getScore());
        }

        // 验证"交易成功"在检索结果中
        boolean foundSuccess = results.stream()
                .anyMatch(doc -> "交易成功".equals(doc.getText())
                        && "SUCCESS".equals(doc.getMetadata().get("name"))
                        && "success".equals(doc.getMetadata().get("code")));
        org.junit.jupiter.api.Assertions.assertTrue(foundSuccess,
                "'交易成功' should be in the search results for query '融资成功状态是什么'");
    }


    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private AppLogSearchTool logSearchTool;

    @Test
    void testAiInvokeLogSearchTool() {
        String userPrompt = "搜一下traceId为abef69813c36423d97d8755d35de89ca.153.17867000988760251最近一小时qa环境的beecloud搜索日志,并且帮我查一下分析查询用了什么sql";
        //String userPrompt = "测试一下";

        ChatClient chatClient = ChatClient.builder(chatModel).build();
        String resp = chatClient.prompt()
                .user(userPrompt)
                .tools(logSearchTool) // 注册工具，AI会读取@Tool注解
                .call()
                .content();
        System.out.println("AI返回结果：" + resp);
    }


    @Autowired
    private SqlQueryTool sqlQueryTool;

    @Autowired
    private SqlQuerySupport sqlQuerySupport;

    @Test
    void testSqlQueryTool() {
        System.out.println("sqlQuery enabled=" + sqlQuerySupport.properties().isEnabled()
                + ", ready=" + sqlQuerySupport.isReady()
                + ", url=" + sqlQuerySupport.properties().getUrl());
        org.junit.jupiter.api.Assertions.assertTrue(sqlQuerySupport.isReady(),
                "SqlQueryTool should be connected. Ensure profile lls is active (application-lls.properties).");

        String id = "1381447308451790883";
        System.out.println("direct ts_transaction=" + sqlQueryTool.runNamedQuery("ts_transaction",
                "{\"value\":\"" + id + "\"}", 20));
        System.out.println("direct ts_asset=" + sqlQueryTool.runNamedQuery("ts_asset",
                "{\"by\":\"id\",\"value\":\"" + id + "\"}", 20));
        System.out.println("direct pay_trade=" + sqlQueryTool.runNamedQuery("pay_trade",
                "{\"by\":\"id\",\"value\":\"" + id + "\"}", 20));

        String userPrompt = "帮忙查一下融资Id1381447308451790883的状态，融资发起方是谁，向哪个资金方银行发起的";
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        String resp = chatClient.prompt()
                .system("你是供应链金融排障助手。必须调用 SqlQueryTool。"
                        + "融资Id 先 runNamedQuery：ts_transaction（value=id）、ts_asset（by=id）、pay_trade（by=id 或 transaction_id）。"
                        + "不要在未成功调用工具时声称查询被禁用。")
                .user(userPrompt)
                .tools(sqlQueryTool)
                .call()
                .content();
        System.out.println("AI返回结果：" + resp);
    }
}
