package com.github.walkvoid.zone.ai.tool.repo;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class RepoReadSupport {

    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            ".java", ".xml", ".yml", ".yaml", ".md", ".sql", ".properties", ".json",
            ".txt", ".html", ".js", ".ts", ".vue", ".kt", ".kts", ".gradle", ".csv");

    private final RepoToolProperties properties;

    public RepoReadSupport(RepoToolProperties properties) {
        this.properties = properties;
    }

    public RepoToolProperties properties() {
        return properties;
    }

    public boolean sandboxExists() {
        return Files.isDirectory(properties.rootPath());
    }

    public List<SearchHit> search(String keyword, String pathPrefix, int maxResults) throws IOException {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("keyword is empty");
        }
        Path root = RepoPathGuard.requireSandboxRoot(properties);
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        Path start = startDir(root, pathPrefix);
        int limit = Math.min(Math.max(maxResults, 1), properties.getMaxSearchResults());
        List<SearchHit> hits = new ArrayList<>();
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.equals(start)) {
                    return FileVisitResult.CONTINUE;
                }
                if (RepoPathGuard.shouldSkipDirectory(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                String rel = RepoPathGuard.toUnix(root, dir);
                if (!RepoPathGuard.isAllowedRelative(rel, properties.normalizedAllowPaths())
                        && !isAncestorOfAllowPath(rel)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (hits.size() >= limit) {
                    return FileVisitResult.TERMINATE;
                }
                if (attrs.size() > properties.getMaxFileBytes()) {
                    return FileVisitResult.CONTINUE;
                }
                String rel = RepoPathGuard.toUnix(root, file);
                if (!RepoPathGuard.isAllowedRelative(rel, properties.normalizedAllowPaths())) {
                    return FileVisitResult.CONTINUE;
                }
                if (RepoPathGuard.isDenied(rel, file.getFileName().toString())) {
                    return FileVisitResult.CONTINUE;
                }
                if (!isSearchableFile(file)) {
                    return FileVisitResult.CONTINUE;
                }
                collectHits(file, rel, needle, hits, limit);
                return hits.size() >= limit ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }
        });
        return hits;
    }

    public FileSlice read(String relativePath, Integer startLine, Integer endLine) throws IOException {
        Path file = RepoPathGuard.resolveReadableFile(properties, relativePath);
        long size = Files.size(file);
        if (size > properties.getMaxFileBytes()) {
            throw new IllegalArgumentException(
                    "file is too large (" + size + " bytes), max=" + properties.getMaxFileBytes());
        }
        byte[] bytes = Files.readAllBytes(file);
        if (containsNul(bytes)) {
            throw new IllegalArgumentException("binary file is not readable: " + relativePath);
        }
        List<String> lines = new String(bytes, StandardCharsets.UTF_8).lines().toList();
        int total = lines.size();
        int start = startLine == null || startLine < 1 ? 1 : startLine;
        int maxLines = Math.max(properties.getMaxReadLines(), 1);
        int requestedEnd = endLine == null ? start + maxLines - 1 : endLine;
        if (requestedEnd < start) {
            throw new IllegalArgumentException("endLine must be >= startLine");
        }
        int end = Math.min(total, Math.min(requestedEnd, start + maxLines - 1));
        if (start > total) {
            throw new IllegalArgumentException("startLine " + start + " is beyond file length " + total);
        }
        StringBuilder content = new StringBuilder();
        for (int i = start; i <= end; i++) {
            content.append(lines.get(i - 1));
            if (i < end) {
                content.append('\n');
            }
        }
        return new FileSlice(RepoPathGuard.toUnix(properties.rootPath(), file), start, end, total, content.toString());
    }

    private Path startDir(Path root, String pathPrefix) {
        if (!StringUtils.hasText(pathPrefix) || ".".equals(pathPrefix.trim())) {
            return root;
        }
        return RepoPathGuard.resolveReadableDir(properties, pathPrefix);
    }

    private boolean isAncestorOfAllowPath(String relativeUnix) {
        String rel = relativeUnix.toLowerCase(Locale.ROOT);
        for (String pattern : properties.normalizedAllowPaths()) {
            String prefix = pattern;
            if (prefix.endsWith("/**") || prefix.endsWith("/*")) {
                prefix = prefix.substring(0, prefix.lastIndexOf('/'));
            }
            if (prefix.startsWith(rel + "/") || prefix.equals(rel)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSearchableFile(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        return TEXT_EXTENSIONS.contains(name.substring(dot));
    }

    private void collectHits(Path file, String rel, String needle, List<SearchHit> hits, int limit) {
        String fileName = file.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).contains(needle) && hits.size() < limit) {
            hits.add(new SearchHit(rel, 0, fileName));
        }
        List<String> lines;
        try {
            byte[] bytes = Files.readAllBytes(file);
            if (containsNul(bytes)) {
                return;
            }
            lines = new String(bytes, StandardCharsets.UTF_8).lines().toList();
        } catch (IOException ex) {
            return;
        }
        for (int i = 0; i < lines.size() && hits.size() < limit; i++) {
            String line = lines.get(i);
            if (line.toLowerCase(Locale.ROOT).contains(needle)) {
                hits.add(new SearchHit(rel, i + 1, truncate(line.trim())));
            }
        }
    }

    private String truncate(String text) {
        int max = Math.max(properties.getMaxHitChars(), 40);
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "...";
    }

    private static boolean containsNul(byte[] bytes) {
        int n = Math.min(bytes.length, 8192);
        for (int i = 0; i < n; i++) {
            if (bytes[i] == 0) {
                return true;
            }
        }
        return false;
    }

    public record SearchHit(String path, int line, String text) {
    }

    public record FileSlice(String path, int startLine, int endLine, int totalLines, String content) {
    }
}
