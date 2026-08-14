package com.github.walkvoid.zone.ai.business.tool.repo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepoPathGuardTest {

    @TempDir
    Path temp;

    @Test
    void allowsWhitelistedJavaFile() throws Exception {
        Path file = temp.resolve("zone-finance/src/Foo.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "class Foo {}");

        Path resolved = RepoPathGuard.resolveReadableFile(props(temp), "zone-finance/src/Foo.java");
        assertTrue(Files.isSameFile(file, resolved));
    }

    @Test
    void rejectsParentEscape() {
        assertThrows(IllegalArgumentException.class, () ->
                RepoPathGuard.resolveInsideRoot(temp, "../secret.txt"));
    }

    @Test
    void rejectsAbsolutePath() {
        assertThrows(IllegalArgumentException.class, () ->
                RepoPathGuard.resolveInsideRoot(temp, "D:/other/Foo.java"));
    }

    @Test
    void rejectsOutsideAllowList() throws Exception {
        Path file = temp.resolve("zone-ai/src/Bar.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "class Bar {}");
        assertThrows(IllegalArgumentException.class, () ->
                RepoPathGuard.resolveReadableFile(props(temp), "zone-ai/src/Bar.java"));
    }

    @Test
    void rejectsEnvAndLlsProperties() throws Exception {
        Path env = temp.resolve("zone-finance/.env");
        Path lls = temp.resolve("zone-finance/application-lls.properties");
        Files.createDirectories(env.getParent());
        Files.writeString(env, "SECRET=1");
        Files.writeString(lls, "password=1");

        RepoToolProperties properties = props(temp);
        assertThrows(IllegalArgumentException.class, () ->
                RepoPathGuard.resolveReadableFile(properties, "zone-finance/.env"));
        assertThrows(IllegalArgumentException.class, () ->
                RepoPathGuard.resolveReadableFile(properties, "zone-finance/application-lls.properties"));
    }

    @Test
    void denyHelpers() {
        assertTrue(RepoPathGuard.isDenied("zone-finance/.env", ".env"));
        assertTrue(RepoPathGuard.isDenied("zone-finance/id_rsa", "id_rsa"));
        assertTrue(RepoPathGuard.isDenied("zone-finance/key.pem", "key.pem"));
        assertFalse(RepoPathGuard.isDenied("zone-finance/src/Foo.java", "Foo.java"));
        assertTrue(RepoPathGuard.isAllowedRelative("zone-finance/src/Foo.java", List.of("zone-finance/**")));
        assertFalse(RepoPathGuard.isAllowedRelative("zone-ai/src/Foo.java", List.of("zone-finance/**")));
    }

    private static RepoToolProperties props(Path root) {
        RepoToolProperties properties = new RepoToolProperties();
        properties.setRoot(root.toString());
        properties.setAllowPaths(List.of("zone-finance/**"));
        return properties;
    }
}
