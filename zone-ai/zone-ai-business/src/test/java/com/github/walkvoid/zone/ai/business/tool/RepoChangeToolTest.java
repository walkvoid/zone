package com.github.walkvoid.zone.ai.business.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoToolProperties;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoWriteMode;
import com.github.walkvoid.zone.ai.business.tool.repo.RepoWriteSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepoChangeToolTest {

    @TempDir
    Path temp;

    private Path javaFile;

    @BeforeEach
    void setUp() throws Exception {
        javaFile = temp.resolve("zone-finance/src/PayListener.java");
        Files.createDirectories(javaFile.getParent());
        Files.writeString(javaFile, """
                package demo;
                public class PayListener {
                    public void onSuccess() {
                        // 放款成功写回
                    }
                }
                """);
    }

    @Test
    void describeWritePolicyWhenEnabled() {
        RepoChangeTool tool = tool(RepoWriteMode.DIFF_FILE);
        JsonNode json = tool.describeWritePolicy();
        assertTrue(json.path("success").asBoolean());
        assertTrue(json.path("writeEnabled").asBoolean());
        assertEquals("DIFF_FILE", json.path("writeMode").asText());
        assertTrue(json.path("sandboxExists").asBoolean());
    }

    @Test
    void applyReplaceDirectWritesSource() throws Exception {
        RepoChangeTool tool = tool(RepoWriteMode.DIRECT);
        JsonNode applied = tool.applyReplace(
                "zone-finance/src/PayListener.java",
                "放款成功写回",
                "放款成功写回（已改）",
                false);
        assertTrue(applied.path("success").asBoolean());
        assertTrue(applied.path("written").asBoolean());
        assertTrue(applied.path("sourceWritten").asBoolean());
        assertEquals("DIRECT", applied.path("writeMode").asText());
        assertTrue(applied.path("addedLines").asInt() >= 1);
        assertTrue(Files.readString(javaFile).contains("放款成功写回（已改）"));
    }

    @Test
    void applyReplaceDiffFileWritesSiblingPatchOnly() throws Exception {
        String original = Files.readString(javaFile);
        RepoChangeTool tool = tool(RepoWriteMode.DIFF_FILE);
        JsonNode applied = tool.applyReplace(
                "zone-finance/src/PayListener.java",
                "放款成功写回",
                "放款成功写回（已改）",
                false);
        assertTrue(applied.path("success").asBoolean());
        assertTrue(applied.path("written").asBoolean());
        assertFalse(applied.path("sourceWritten").asBoolean());
        assertEquals("DIFF_FILE", applied.path("writeMode").asText());
        String patchFile = applied.path("patchFile").asText();
        assertTrue(patchFile.contains("fix_"));
        assertTrue(patchFile.endsWith("_PayListener.patch"));
        assertEquals(original, Files.readString(javaFile));
        Path patchPath = temp.resolve(patchFile);
        assertTrue(Files.exists(patchPath));
        String patch = Files.readString(patchPath);
        assertTrue(patch.startsWith("diff --git "));
        assertTrue(patch.contains("-        // 放款成功写回"));
        assertTrue(patch.contains("+        // 放款成功写回（已改）"));
        try (Stream<Path> files = Files.list(javaFile.getParent())) {
            long patches = files.filter(p -> p.getFileName().toString().endsWith(".patch")).count();
            assertEquals(1, patches);
        }
    }

    @Test
    void applyPatchCreatesNewFileDirect() throws Exception {
        RepoChangeTool tool = tool(RepoWriteMode.DIRECT);
        String path = "zone-finance/src/NewHelper.java";
        String content = "public class NewHelper { }\n";
        JsonNode applied = tool.applyPatch(path, content);
        assertTrue(applied.path("success").asBoolean());
        assertTrue(applied.path("newFile").asBoolean());
        assertTrue(applied.path("sourceWritten").asBoolean());
        assertTrue(Files.exists(temp.resolve(path)));
    }

    @Test
    void rejectsConfigAndOutsideAllowList() {
        RepoChangeTool tool = tool(RepoWriteMode.DIFF_FILE);
        JsonNode denied = tool.applyPatch("zone-finance/application.properties", "x=1\n");
        assertFalse(denied.path("success").asBoolean());

        JsonNode outside = tool.applyPatch("zone-ai/src/Hidden.java", "class Hidden {}\n");
        assertFalse(outside.path("success").asBoolean());
    }

    private RepoChangeTool tool(RepoWriteMode mode) {
        RepoToolProperties properties = new RepoToolProperties();
        properties.setRoot(temp.toString());
        properties.setAllowPaths(List.of("zone-finance/**"));
        properties.setWriteEnabled(true);
        properties.setWriteAllowPaths(List.of("zone-finance/**"));
        properties.setWriteMode(mode);
        properties.setMaxPatchLines(400);
        return new RepoChangeTool(new RepoWriteSupport(properties));
    }
}
