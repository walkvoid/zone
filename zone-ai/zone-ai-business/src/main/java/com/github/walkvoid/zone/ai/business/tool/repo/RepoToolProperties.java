package com.github.walkvoid.zone.ai.business.tool.repo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 代码沙箱只读配置。前缀 {@code zone.ai.tool.repo}。
 * Agent 只读这份副本，不直接碰开发者正在改的工作区。
 */
@ConfigurationProperties(prefix = "zone.ai.tool.repo")
public class RepoToolProperties {

    private boolean enabled = true;

    /**
     * 沙箱仓库根目录，例如 {@code D:/ai-sandbox/zone}。
     */
    private String root = "D:/ai-sandbox/zone";

    private String name = "zone";

    /**
     * 相对 root 的路径白名单，支持 {@code dir/**}。
     */
    private List<String> allowPaths = new ArrayList<>(List.of("zone-finance/**"));

    private int maxReadLines = 400;
    private int maxSearchResults = 50;
    private int maxFileBytes = 524288;
    private int maxHitChars = 240;

    /** 是否允许写入沙箱（默认关，避免误改）。 */
    private boolean writeEnabled = false;

    /**
     * 可写路径白名单，默认可空；为空时回退到 {@link #allowPaths}。
     */
    private List<String> writeAllowPaths = new ArrayList<>();

    /** 单次 patch 最大变更行数（增+删）。 */
    private int maxPatchLines = 400;

    /** 单次 patch 最大文件字节数。 */
    private int maxPatchBytes = 524288;

    /**
     * apply 落盘方式：{@link RepoWriteMode#DIFF_FILE} 只写同级 .patch；
     * {@link RepoWriteMode#DIRECT} 覆盖源文件。
     */
    private RepoWriteMode writeMode = RepoWriteMode.DIFF_FILE;

    /** unified diff 上下文行数。 */
    private int patchContextLines = 3;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAllowPaths() {
        return allowPaths;
    }

    public void setAllowPaths(List<String> allowPaths) {
        this.allowPaths = allowPaths;
    }

    public int getMaxReadLines() {
        return maxReadLines;
    }

    public void setMaxReadLines(int maxReadLines) {
        this.maxReadLines = maxReadLines;
    }

    public int getMaxSearchResults() {
        return maxSearchResults;
    }

    public void setMaxSearchResults(int maxSearchResults) {
        this.maxSearchResults = maxSearchResults;
    }

    public int getMaxFileBytes() {
        return maxFileBytes;
    }

    public void setMaxFileBytes(int maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
    }

    public int getMaxHitChars() {
        return maxHitChars;
    }

    public void setMaxHitChars(int maxHitChars) {
        this.maxHitChars = maxHitChars;
    }

    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    public void setWriteEnabled(boolean writeEnabled) {
        this.writeEnabled = writeEnabled;
    }

    public List<String> getWriteAllowPaths() {
        return writeAllowPaths;
    }

    public void setWriteAllowPaths(List<String> writeAllowPaths) {
        this.writeAllowPaths = writeAllowPaths;
    }

    public int getMaxPatchLines() {
        return maxPatchLines;
    }

    public void setMaxPatchLines(int maxPatchLines) {
        this.maxPatchLines = maxPatchLines;
    }

    public int getMaxPatchBytes() {
        return maxPatchBytes;
    }

    public void setMaxPatchBytes(int maxPatchBytes) {
        this.maxPatchBytes = maxPatchBytes;
    }

    public RepoWriteMode getWriteMode() {
        return writeMode == null ? RepoWriteMode.DIFF_FILE : writeMode;
    }

    public void setWriteMode(RepoWriteMode writeMode) {
        this.writeMode = writeMode;
    }

    public int getPatchContextLines() {
        return patchContextLines;
    }

    public void setPatchContextLines(int patchContextLines) {
        this.patchContextLines = patchContextLines;
    }

    public Path rootPath() {
        String value = StringUtils.hasText(root) ? root.trim() : "D:/ai-sandbox/zone";
        return Paths.get(value).toAbsolutePath().normalize();
    }

    public String displayName() {
        return StringUtils.hasText(name) ? name.trim() : "zone";
    }

    public List<String> normalizedAllowPaths() {
        List<String> result = new ArrayList<>();
        if (allowPaths == null) {
            return result;
        }
        for (String raw : allowPaths) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            result.add(raw.trim().replace('\\', '/').toLowerCase(Locale.ROOT));
        }
        return result;
    }

    public List<String> normalizedWriteAllowPaths() {
        List<String> source = writeAllowPaths;
        if (source == null || source.isEmpty()) {
            source = allowPaths;
        }
        List<String> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (String raw : source) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            result.add(raw.trim().replace('\\', '/').toLowerCase(Locale.ROOT));
        }
        return result;
    }
}
