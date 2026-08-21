package com.github.walkvoid.zone.ai.model.enums;

/**
 * Prompt模板状态枚举
 *
 * @author walkvoid
 */
public enum PromptStatusEnum {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer key;
    private final String desc;

    PromptStatusEnum(Integer key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public Integer getKey() { return key; }

    public String getDesc() { return desc; }
}
