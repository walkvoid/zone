package com.github.walkvoid.zone.ai.business.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoReadSupport;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoToolProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepoReadToolTest {

    @TempDir
    Path temp;

    private RepoReadTool tool;

    @BeforeEach
    void setUp() throws Exception {
        Path javaFile = temp.resolve("zone-finance/src/PayListener.java");
        Files.createDirectories(javaFile.getParent());
        Files.writeString(javaFile, """
                package demo;
                public class PayListener {
                    public void onSuccess() {
                        // 放款成功写回
                    }
                }
                """);
        Path hidden = temp.resolve("zone-ai/src/Hidden.java");
        Files.createDirectories(hidden.getParent());
        Files.writeString(hidden, "class Hidden { String token = \"secret\"; }");
        Path env = temp.resolve("zone-finance/.env");
        Files.writeString(env, "TOKEN=abc");

        Path longFile = temp.resolve("zone-finance/src/Long.java");
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 1; i <= 500; i++) {
            joiner.add("line-" + i);
        }
        Files.writeString(longFile, joiner.toString());

        RepoToolProperties properties = new RepoToolProperties();
        properties.setRoot(temp.toString());
        properties.setAllowPaths(List.of("zone-finance/**"));
        properties.setMaxReadLines(400);
        tool = new RepoReadTool(new RepoReadSupport(properties));
    }

    @Test
    void listReposReportsSandbox() {
        JsonNode json = tool.listRepos();
        assertTrue(json.path("success").asBoolean());
        assertTrue(json.path("repos").get(0).path("exists").asBoolean());
        assertEquals("zone-finance/**", json.path("repos").get(0).path("allowPaths").get(0).asText());
    }

    @Test
    void searchFindsAllowedFileAndSkipsOthers() {
        JsonNode json = tool.searchCode("放款成功写回", null, 20);
        assertTrue(json.path("success").asBoolean());
        assertTrue(json.path("returned").asInt() >= 1);
        assertTrue(json.path("hits").get(0).path("path").asText().contains("PayListener.java"));

        JsonNode hidden = tool.searchCode("class Hidden", null, 20);
        assertTrue(hidden.path("success").asBoolean());
        assertEquals(0, hidden.path("returned").asInt());

        JsonNode env = tool.searchCode("TOKEN=abc", null, 20);
        assertEquals(0, env.path("returned").asInt());
    }

    @Test
    void readCapsAtMaxLinesAndRejectsDenied() {
        JsonNode slice = tool.readSourceFile("zone-finance/src/Long.java", 1, null);
        assertTrue(slice.path("success").asBoolean());
        assertEquals(1, slice.path("startLine").asInt());
        assertEquals(400, slice.path("endLine").asInt());
        assertEquals(500, slice.path("totalLines").asInt());

        JsonNode denied = tool.readSourceFile("zone-finance/.env", 1, 10);
        assertFalse(denied.path("success").asBoolean());

        JsonNode escape = tool.readSourceFile("../zone-ai/src/Hidden.java", 1, 10);
        assertFalse(escape.path("success").asBoolean());
    }
}
