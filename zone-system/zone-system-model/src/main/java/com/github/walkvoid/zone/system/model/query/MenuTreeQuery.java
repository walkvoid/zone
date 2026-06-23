package com.github.walkvoid.zone.system.model.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜单树查询参数
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
public class MenuTreeQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否只查询启用的菜单 */
    private Boolean onlyEnabled = true;

    /** 是否只查询可见菜单 */
    private Boolean onlyVisible = true;
}
