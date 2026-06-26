package com.github.walkvoid.zone.finance.api.enums;

import com.github.walkvoid.wvframework.models.BaseEnum;

/**
 * 股票板块枚举
 * @author walkvoid
 * @version 1.0
 * @date 2026/6/25
 * @desc 沪市主板、科创板、深市主板、创业板、北交所
 */
public enum StockPlateEnum implements BaseEnum<Integer> {

    SH_MAIN(1, "沪市主板"),
    SH_STAR(2, "科创板"),
    SZ_MAIN(3, "深市主板"),
    SZ_CHINEXT(4, "创业板"),
    BJ(5, "北交所");

    private final Integer key;
    private final String desc;

    StockPlateEnum(Integer key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public Integer getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public static StockPlateEnum getByKey(Integer key) {
        for (StockPlateEnum e : values()) {
            if (e.getKey().equals(key)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toString0();
    }
}
