package com.github.walkvoid.zone.ai.business.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.walkvoid.wvframework.utils.JsonNodeUtils;
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
        assertTrue(JsonNodeUtils.asBoolean(json, false, "success"));
        assertTrue(JsonNodeUtils.asBoolean(JsonNodeUtils.path(json, "repos").get(0), false, "exists"));
        assertEquals("zone-finance/**", JsonNodeUtils.asText(JsonNodeUtils.path(json, "repos").get(0).path("allowPaths").get(0)));
    }

    @Test
    void searchFindsAllowedFileAndSkipsOthers() {
        JsonNode json = tool.searchCode("放款成功写回", null, 20);
        assertTrue(JsonNodeUtils.asBoolean(json, false, "success"));
        assertTrue(JsonNodeUtils.asInt(json, 0, "returned") >= 1);
        assertTrue(JsonNodeUtils.asText(JsonNodeUtils.path(json, "hits").get(0), "path").contains("PayListener.java"));

        JsonNode hidden = tool.searchCode("class Hidden", null, 20);
        assertTrue(JsonNodeUtils.asBoolean(hidden, false, "success"));
        assertEquals(0, JsonNodeUtils.asInt(hidden, -1, "returned"));

        JsonNode env = tool.searchCode("TOKEN=abc", null, 20);
        assertEquals(0, JsonNodeUtils.asInt(env, -1, "returned"));
    }

    @Test
    void readCapsAtMaxLinesAndRejectsDenied() {
        JsonNode slice = tool.readSourceFile("zone-finance/src/Long.java", 1, null);
        assertTrue(JsonNodeUtils.asBoolean(slice, false, "success"));
        assertEquals(1, JsonNodeUtils.asInt(slice, 0, "startLine"));
        assertEquals(400, JsonNodeUtils.asInt(slice, 0, "endLine"));
        assertEquals(500, JsonNodeUtils.asInt(slice, 0, "totalLines"));

        JsonNode denied = tool.readSourceFile("zone-finance/.env", 1, 10);
        assertFalse(JsonNodeUtils.asBoolean(denied, false, "success"));

        JsonNode escape = tool.readSourceFile("../zone-ai/src/Hidden.java", 1, 10);
        assertFalse(JsonNodeUtils.asBoolean(escape, false, "success"));
    }
}
