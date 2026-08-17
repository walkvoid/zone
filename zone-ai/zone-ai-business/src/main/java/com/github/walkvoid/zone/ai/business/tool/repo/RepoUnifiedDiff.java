package com.github.walkvoid.zone.ai.business.tool.repo;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.util.ArrayList;
import java.util.List;

/**
 * 用 java-diff-utils 生成 Git 风格 unified diff。
 */
final class RepoUnifiedDiff {

    private RepoUnifiedDiff() {
    }

    static DiffResult of(String relativeUnix, String oldContent, String newContent, int contextLines) {
        List<String> oldLines = splitLines(oldContent);
        List<String> newLines = splitLines(newContent);
        Patch<String> patch = DiffUtils.diff(oldLines, newLines);
        int added = 0;
        int removed = 0;
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            switch (delta.getType()) {
                case INSERT -> added += delta.getTarget().size();
                case DELETE -> removed += delta.getSource().size();
                case CHANGE -> {
                    added += delta.getTarget().size();
                    removed += delta.getSource().size();
                }
                case EQUAL -> {
                    // ignore
                }
            }
        }
        int context = Math.max(contextLines, 0);
        List<String> unified = UnifiedDiffUtils.generateUnifiedDiff(
                "a/" + relativeUnix,
                "b/" + relativeUnix,
                oldLines,
                patch,
                context);
        List<String> withHeader = new ArrayList<>();
        withHeader.add("diff --git a/" + relativeUnix + " b/" + relativeUnix);
        withHeader.addAll(unified);
        String text = String.join("\n", withHeader);
        if (!text.endsWith("\n")) {
            text = text + "\n";
        }
        return new DiffResult(added, removed, text);
    }

    static String preview(String unifiedDiff, int maxLines) {
        if (unifiedDiff == null || unifiedDiff.isEmpty()) {
            return "";
        }
        String[] lines = unifiedDiff.split("\n", -1);
        if (lines.length <= maxLines) {
            return unifiedDiff.trim();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            sb.append(lines[i]).append('\n');
        }
        sb.append("... (diff truncated)");
        return sb.toString().trim();
    }

    private static List<String> splitLines(String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        return List.of(normalized.split("\n", -1));
    }

    record DiffResult(int addedLines, int removedLines, String unifiedDiff) {
    }
}
