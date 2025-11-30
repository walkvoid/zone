package com.github.walkvoid.zone.user.model.enums;

/**
 * 用户状态枚举类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 包含key和desc属性，定义用户的三种状态：初始化、已激活和已禁用
 */
public enum UserInfoStatusEnum {

    /**
     * 初始化状态
     */
    INIT("init", "初始化"),
    /**
     * 已激活状态
     */
    ACTIVE("active", "已激活"),
    /**
     * 已禁用状态
     */
    DISABLE("disable", "已禁用");

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
    UserInfoStatusEnum(String key, String desc) {
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
    public static UserInfoStatusEnum getByKey(String key) {
        for (UserInfoStatusEnum statusEnum : values()) {
            if (statusEnum.getKey().equals(key)) {
                return statusEnum;
            }
        }
        return null;
    }
}