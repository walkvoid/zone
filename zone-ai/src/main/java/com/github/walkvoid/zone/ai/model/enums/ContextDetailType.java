package com.github.walkvoid.zone.ai.model.enums;

/**
 * 上下文包明细类型
 */
public enum ContextDetailType {
    /** 文件 / 文档 */
    FILE,
    /** 源码 / 分支 */
    CODE,
    /** 其它（含库表说明、自由文本等） */
    OTHER;

    public static ContextDetailType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ContextDetailType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
