package com.github.walkvoid.zone.user.model.enums;

import com.github.walkvoid.wvframework.dao.BaseEnum;

/**
 * 用户性别枚举类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 0=未知，1=男，2=女
 */
public enum UserGenderEnum implements BaseEnum<Integer> {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),
    /**
     * 男
     */
    MALE(1, "男"),
    /**
     * 女
     */
    FEMALE(2, "女");

    /**
     * 枚举值
     */
    private final Integer key;
    /**
     * 枚举描述
     */
    private final String desc;

    /**
     * 构造函数
     * @param key 枚举值
     * @param desc 枚举描述
     */
    UserGenderEnum(Integer key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    /**
     * 获取枚举值
     * @return 枚举值
     */
    @Override
    public Integer getKey() {
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
    public static UserGenderEnum getByKey(Integer key) {
        for (UserGenderEnum genderEnum : values()) {
            if (genderEnum.getKey().equals(key)) {
                return genderEnum;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
