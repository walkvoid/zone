package com.github.walkvoid.zone.ai.business.agent.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentAuditJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void truncateKeepsShortText() {
        assertEquals("hello", AgentAuditJson.truncate("hello", 8192));
        assertEquals("", AgentAuditJson.truncate("", 8192));
        assertEquals(null, AgentAuditJson.truncate(null, 8192));
    }

    @Test
    void truncateCutsUtf8BytesAndAppendsEllipsis() {
        String text = "a".repeat(400);
        String cut = AgentAuditJson.truncate(text, 256);
        assertTrue(cut.endsWith("…"));
        assertTrue(cut.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 256);
        assertTrue(cut.length() < text.length());
    }

    @Test
    void summarizeUsesErrorWhenFailed() throws Exception {
        String summary = AgentAuditJson.summarize(null, "searchLogs", false, "timeout");
        assertTrue(summary.contains("失败"));
        assertTrue(summary.contains("timeout"));
    }

    @Test
    void summarizeReadsRowCountAndPath() throws Exception {
        var node = mapper.readTree("{\"success\":true,\"rowCount\":3,\"path\":\"PayListener.java\"}");
        String summary = AgentAuditJson.summarize(node, "query", true, null);
        assertTrue(summary.contains("成功"));
        assertTrue(summary.contains("rowCount=3"));
        assertTrue(summary.contains("PayListener.java"));
    }
}
