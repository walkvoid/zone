package com.github.walkvoid.zone.system.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜单树查询参数 DTO
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
public class MenuTreeQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否只查询可见菜单（visible=1） */
    private Boolean onlyVisible = true;
}
