package com.github.walkvoid.zone.ai.business.tool.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RepoWriteSupport {

    private static final int DIFF_PREVIEW_LINES = 80;
    private static final DateTimeFormatter PATCH_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private final RepoToolProperties properties;
    private final Clock clock;

    @Autowired
    public RepoWriteSupport(RepoToolProperties properties) {
        this(properties, Clock.systemDefaultZone());
    }

    RepoWriteSupport(RepoToolProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public RepoToolProperties properties() {
        return properties;
    }

    public boolean sandboxExists() {
        return Files.isDirectory(properties.rootPath());
    }

    PatchPreview preparePatch(String relativePath, String newContent) throws IOException {
        validateWriteReady();
        if (!StringUtils.hasText(relativePath)) {
            throw new IllegalArgumentException("path is empty");
        }
        if (newContent == null) {
            throw new IllegalArgumentException("newContent is null");
        }
        validateContentSize(newContent);
        Path file = RepoPathGuard.resolveWritableFile(properties, relativePath);
        String rel = RepoPathGuard.toUnix(properties.rootPath(), file);
        String oldContent = Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8) : "";
        boolean newFile = !Files.exists(file);
        RepoUnifiedDiff.DiffResult diff = RepoUnifiedDiff.of(
                rel, oldContent, newContent, properties.getPatchContextLines());
        enforcePatchLimits(diff);
        if (diff.addedLines() == 0 && diff.removedLines() == 0) {
            throw new IllegalArgumentException("no changes to apply: " + rel);
        }
        return new PatchPreview(
                rel,
                newFile,
                diff.addedLines(),
                diff.removedLines(),
                RepoUnifiedDiff.preview(diff.unifiedDiff(), DIFF_PREVIEW_LINES),
                diff.unifiedDiff(),
                newContent);
    }

    PatchPreview prepareReplace(String relativePath, String oldText, String newText, boolean replaceAll)
            throws IOException {
        validateWriteReady();
        if (!StringUtils.hasText(oldText)) {
            throw new IllegalArgumentException("oldText is empty");
        }
        if (newText == null) {
            throw new IllegalArgumentException("newText is null");
        }
        Path file = RepoPathGuard.resolveWritableFile(properties, relativePath);
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("File does not exist: " + relativePath);
        }
        String oldContent = Files.readString(file, StandardCharsets.UTF_8);
        int count = countOccurrences(oldContent, oldText);
        if (count == 0) {
            throw new IllegalArgumentException("oldText not found in file: " + relativePath);
        }
        if (!replaceAll && count > 1) {
            throw new IllegalArgumentException("oldText matched " + count + " times; set replaceAll=true or narrow oldText");
        }
        String updated = replaceAll ? oldContent.replace(oldText, newText) : replaceFirstLiteral(oldContent, oldText, newText);
        return preparePatch(relativePath, updated);
    }

    public ApplyResult applyPatch(String relativePath, String newContent) throws IOException {
        PatchPreview preview = preparePatch(relativePath, newContent);
        return persist(preview);
    }

    public ApplyResult applyReplace(String relativePath, String oldText, String newText, boolean replaceAll)
            throws IOException {
        PatchPreview preview = prepareReplace(relativePath, oldText, newText, replaceAll);
        return persist(preview);
    }

    private ApplyResult persist(PatchPreview preview) throws IOException {
        RepoWriteMode mode = properties.getWriteMode();
        if (mode == RepoWriteMode.DIFF_FILE) {
            String patchRel = writeSiblingPatch(preview.path(), preview.unifiedDiff());
            return new ApplyResult(
                    preview.path(),
                    preview.newFile(),
                    preview.addedLines(),
                    preview.removedLines(),
                    true,
                    false,
                    mode,
                    patchRel);
        }
        Path file = RepoPathGuard.resolveWritableFile(properties, preview.path());
        Files.createDirectories(file.getParent());
        Files.writeString(file, preview.newContent(), StandardCharsets.UTF_8);
        return new ApplyResult(
                preview.path(),
                preview.newFile(),
                preview.addedLines(),
                preview.removedLines(),
                true,
                true,
                mode,
                null);
    }

    private String writeSiblingPatch(String sourceRelativeUnix, String unifiedDiff) throws IOException {
        Path source = RepoPathGuard.resolveWritableFile(properties, sourceRelativeUnix);
        Path dir = source.getParent();
        Files.createDirectories(dir);
        String baseName = stripExtension(source.getFileName().toString());
        String stamp = LocalDateTime.now(clock).format(PATCH_STAMP);
        String fileName = "fix_" + stamp + "_" + baseName + ".patch";
        Path patch = uniquePatchPath(dir, fileName, baseName, stamp);
        String patchRel = RepoPathGuard.toUnix(properties.rootPath(), patch);
        Path guarded = RepoPathGuard.resolveWritableFile(properties, patchRel);
        Files.writeString(guarded, unifiedDiff, StandardCharsets.UTF_8);
        return RepoPathGuard.toUnix(properties.rootPath(), guarded);
    }

    private Path uniquePatchPath(Path dir, String fileName, String baseName, String stamp) {
        Path patch = dir.resolve(fileName);
        int suffix = 2;
        while (Files.exists(patch)) {
            patch = dir.resolve("fix_" + stamp + "_" + baseName + "_" + suffix + ".patch");
            suffix++;
        }
        return patch;
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0) {
            return fileName;
        }
        return fileName.substring(0, dot);
    }

    private void validateWriteReady() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Repo tool is disabled (zone.ai.tool.repo.enabled=false).");
        }
        if (!properties.isWriteEnabled()) {
            throw new IllegalStateException("Repo write is disabled (zone.ai.tool.repo.write-enabled=false).");
        }
        RepoPathGuard.requireSandboxRoot(properties);
    }

    private void validateContentSize(String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > properties.getMaxPatchBytes()) {
            throw new IllegalArgumentException(
                    "content is too large (" + bytes.length + " bytes), max=" + properties.getMaxPatchBytes());
        }
    }

    private void enforcePatchLimits(RepoUnifiedDiff.DiffResult diff) {
        int changed = diff.addedLines() + diff.removedLines();
        if (changed > properties.getMaxPatchLines()) {
            throw new IllegalArgumentException(
                    "patch too large (" + changed + " changed lines), max=" + properties.getMaxPatchLines());
        }
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) >= 0) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    private static String replaceFirstLiteral(String haystack, String needle, String replacement) {
        int idx = haystack.indexOf(needle);
        if (idx < 0) {
            return haystack;
        }
        return haystack.substring(0, idx) + replacement + haystack.substring(idx + needle.length());
    }

    public record PatchPreview(
            String path,
            boolean newFile,
            int addedLines,
            int removedLines,
            String diffPreview,
            String unifiedDiff,
            String newContent) {
    }

    public record ApplyResult(
            String path,
            boolean newFile,
            int addedLines,
            int removedLines,
            boolean written,
            boolean sourceWritten,
            RepoWriteMode writeMode,
            String patchFile) {
    }
}
