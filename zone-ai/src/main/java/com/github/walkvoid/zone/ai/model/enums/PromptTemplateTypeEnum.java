package com.github.walkvoid.zone.ai.model.enums;

import com.github.walkvoid.wvframework.models.BaseEnum;

public enum PromptTemplateTypeEnum implements BaseEnum<String> {


    DEFAULT_SYSTEM("default_system", "默认系统提示词"),
    DEFAULT_USER("default_user", "默认用户提示词");

    private final String key;
    private final String desc;

    PromptTemplateTypeEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() { return key; }

    public String getDesc() { return desc; }

    @Override
    public String toString() {
        return BaseEnum.super.toString0();
    }


}
