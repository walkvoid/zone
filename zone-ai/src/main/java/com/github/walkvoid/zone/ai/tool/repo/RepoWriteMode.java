package com.github.walkvoid.zone.ai.tool.repo;

/**
 * 改代码 apply 时的落盘方式。
 */
public enum RepoWriteMode {

    /**
     * 在源文件同级目录写出 unified diff（.patch），不修改源文件。
     */
    DIFF_FILE,

    /**
     * 用新内容覆盖沙箱内源文件。
     */
    DIRECT
}
