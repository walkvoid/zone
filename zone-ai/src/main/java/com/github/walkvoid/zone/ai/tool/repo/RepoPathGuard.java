package com.github.walkvoid.zone.ai.tool.repo;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 沙箱路径校验：限制在 root + 白名单内，禁止逃逸和密钥文件。
 */
public final class RepoPathGuard {

    static final Set<String> SKIP_DIR_NAMES = Set.of(
            "target", "build", "dist", "out", "node_modules", ".git", ".idea", ".cursor", ".svn");

    private static final Set<String> DENIED_NAMES = Set.of(
            ".env",
            ".env.local",
            ".env.production",
            "application-lls.properties",
            "credentials.json",
            "id_rsa",
            "id_rsa.pub",
            "id_ed25519",
            "id_ed25519.pub");

    private static final Set<String> DENIED_EXTENSIONS = Set.of(
            ".pem", ".key", ".p12", ".jks", ".keystore", ".secret", ".pfx");

    static final Set<String> WRITABLE_EXTENSIONS = Set.of(
            ".java", ".xml", ".yml", ".yaml", ".md", ".sql", ".json",
            ".txt", ".html", ".js", ".ts", ".vue", ".kt", ".kts", ".gradle", ".csv",
            ".properties", ".patch");

    private static final Pattern DENIED_NAME_PATTERN = Pattern.compile(
            "(?i)^(\\.env\\b.*|.*secret.*|.*credentials.*|id_rsa.*|id_ed25519.*|application-lls.*)$");

    private RepoPathGuard() {
    }

    public static Path requireSandboxRoot(RepoToolProperties properties) {
        Path root = properties.rootPath();
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException(
                    "Sandbox repo not found: " + root
                            + ". Clone or git worktree the zone repo to this path first.");
        }
        return root;
    }

    public static Path resolveReadableFile(RepoToolProperties properties, String relativePath) {
        Path root = requireSandboxRoot(properties);
        Path resolved = resolveInsideRoot(root, relativePath);
        String rel = toUnix(root, resolved);
        requireAllowed(rel, properties.normalizedAllowPaths());
        requireNotDenied(rel, resolved.getFileName().toString());
        if (!Files.isRegularFile(resolved)) {
            throw new IllegalArgumentException("Not a regular file: " + rel);
        }
        return resolved;
    }

    public static Path resolveReadableDir(RepoToolProperties properties, String relativePath) {
        Path root = requireSandboxRoot(properties);
        if (!StringUtils.hasText(relativePath) || ".".equals(relativePath.trim())) {
            return root;
        }
        Path resolved = resolveInsideRoot(root, relativePath);
        String rel = toUnix(root, resolved);
        requireAllowed(rel, properties.normalizedAllowPaths());
        if (!Files.isDirectory(resolved)) {
            throw new IllegalArgumentException("Not a directory: " + rel);
        }
        return resolved;
    }

    public static Path resolveWritableFile(RepoToolProperties properties, String relativePath) {
        Path root = requireSandboxRoot(properties);
        Path resolved = resolveInsideRoot(root, relativePath);
        String rel = toUnix(root, resolved);
        requireAllowedWrite(rel, properties.normalizedWriteAllowPaths());
        requireNotDeniedForWrite(rel, resolved.getFileName().toString());
        if (Files.exists(resolved) && !Files.isRegularFile(resolved)) {
            throw new IllegalArgumentException("Not a regular file: " + rel);
        }
        if (!Files.exists(resolved)) {
            requireWritableExtension(resolved.getFileName().toString());
        }
        return resolved;
    }

    public static boolean isWritableExtension(String fileName) {
        String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        return WRITABLE_EXTENSIONS.contains(name.substring(dot));
    }

    public static boolean isAllowedRelative(String relativeUnix, List<String> allowPaths) {
        if (!StringUtils.hasText(relativeUnix)) {
            return false;
        }
        String rel = relativeUnix.replace('\\', '/').toLowerCase(Locale.ROOT);
        if (rel.startsWith("./")) {
            rel = rel.substring(2);
        }
        for (String pattern : allowPaths) {
            if (matchesPattern(rel, pattern)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDenied(String relativeUnix, String fileName) {
        String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        String rel = relativeUnix == null ? "" : relativeUnix.replace('\\', '/').toLowerCase(Locale.ROOT);
        if (rel.startsWith(".git/") || rel.contains("/.git/")) {
            return true;
        }
        if (DENIED_NAMES.contains(name)) {
            return true;
        }
        for (String ext : DENIED_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return DENIED_NAME_PATTERN.matcher(name).matches()
                || DENIED_NAME_PATTERN.matcher(rel).matches();
    }

    public static boolean shouldSkipDirectory(Path dir) {
        Path name = dir.getFileName();
        return name != null && SKIP_DIR_NAMES.contains(name.toString().toLowerCase(Locale.ROOT));
    }

    public static String toUnix(Path root, Path file) {
        Path rootN = root.toAbsolutePath().normalize();
        Path fileN = file.toAbsolutePath().normalize();
        return rootN.relativize(fileN).toString().replace('\\', '/');
    }

    static Path resolveInsideRoot(Path root, String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new IllegalArgumentException("path is empty");
        }
        String raw = relativePath.trim().replace('\\', '/');
        if (raw.startsWith("/") || raw.matches("^[A-Za-z]:/.*")) {
            throw new IllegalArgumentException("absolute path is not allowed: " + relativePath);
        }
        if (raw.contains("..")) {
            throw new IllegalArgumentException("path must not contain '..': " + relativePath);
        }
        Path requested = Path.of(raw);
        if (requested.isAbsolute()) {
            throw new IllegalArgumentException("absolute path is not allowed: " + relativePath);
        }
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(requested).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("path escapes sandbox root: " + relativePath);
        }
        try {
            if (Files.exists(resolved) && Files.exists(normalizedRoot)) {
                Path realRoot = normalizedRoot.toRealPath();
                Path realResolved = resolved.toRealPath();
                if (!realResolved.startsWith(realRoot)) {
                    throw new IllegalArgumentException("path escapes sandbox root: " + relativePath);
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot resolve path: " + relativePath);
        }
        return resolved;
    }

    static void requireAllowed(String relativeUnix, List<String> allowPaths) {
        if (allowPaths == null || allowPaths.isEmpty()) {
            throw new IllegalArgumentException("repo allow-paths is empty; nothing can be read");
        }
        if (!isAllowedRelative(relativeUnix, allowPaths)) {
            throw new IllegalArgumentException("path is not in allow-list: " + relativeUnix);
        }
    }

    static void requireNotDenied(String relativeUnix, String fileName) {
        if (isDenied(relativeUnix, fileName)) {
            throw new IllegalArgumentException("reading this file is forbidden: " + relativeUnix);
        }
    }

    static void requireNotDeniedForWrite(String relativeUnix, String fileName) {
        requireNotDenied(relativeUnix, fileName);
        String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if ("pom.xml".equals(name) || "application.properties".equals(name)) {
            throw new IllegalArgumentException("writing this file is forbidden: " + relativeUnix);
        }
        if (name.startsWith("application-") && name.endsWith(".properties")) {
            throw new IllegalArgumentException("writing config file is forbidden: " + relativeUnix);
        }
    }

    static void requireAllowedWrite(String relativeUnix, List<String> allowPaths) {
        if (allowPaths == null || allowPaths.isEmpty()) {
            throw new IllegalArgumentException("repo write-allow-paths is empty; nothing can be written");
        }
        if (!isAllowedRelative(relativeUnix, allowPaths)) {
            throw new IllegalArgumentException("path is not in write allow-list: " + relativeUnix);
        }
    }

    static void requireWritableExtension(String fileName) {
        if (!isWritableExtension(fileName)) {
            throw new IllegalArgumentException("file extension is not writable: " + fileName);
        }
    }

    static boolean matchesPattern(String relativeLower, String patternLower) {
        String pattern = patternLower.replace('\\', '/');
        String rel = relativeLower.replace('\\', '/');
        if ("**".equals(pattern) || "/**".equals(pattern) || "*".equals(pattern)) {
            return true;
        }
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            if (!StringUtils.hasText(prefix)) {
                return true;
            }
            return rel.equals(prefix) || rel.startsWith(prefix + "/");
        }
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            if (rel.equals(prefix)) {
                return true;
            }
            if (!rel.startsWith(prefix + "/")) {
                return false;
            }
            return rel.indexOf('/', prefix.length() + 1) < 0;
        }
        return rel.equals(pattern) || rel.startsWith(pattern + "/");
    }
}
