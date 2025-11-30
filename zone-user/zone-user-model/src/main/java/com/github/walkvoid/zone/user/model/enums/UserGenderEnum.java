package com.github.walkvoid.zone.user.model.enums;

/**
 * 用户性别枚举类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 包含key和desc属性，定义用户的三种性别：男、女和未知
 */
public enum UserGenderEnum {
    /**
     * 男
     */
    MALE("male", "男"),
    /**
     * 女
     */
    FEMALE("female", "女"),
    /**
     * 未知
     */
    UNKNOWN("unknown", "未知");

    /**
     * 枚举值
     */
    private final String key;
    /**
     * 枚举描述
     */
    private final String desc;

    /**
     * 构造函数
     * @param key 枚举值
     * @param desc 枚举描述
     */
    UserGenderEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    /**
     * 获取枚举值
     * @return 枚举值
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取枚举描述
     * @return 枚举描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据key获取枚举
     * @param key 枚举值
     * @return 对应的枚举
     */
    public static UserGenderEnum getByKey(String key) {
        for (UserGenderEnum genderEnum : values()) {
            if (genderEnum.getKey().equals(key)) {
                return genderEnum;
            }
        }
        return null;
    }
}