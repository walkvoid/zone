package com.github.walkvoid.zone.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.wvframework.models.BooleanEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单实体类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
@TableName("menu")
public class Menu implements Serializable {
    private static final long serialVersionUID = -8527693140486687132L;

    @TableId
    private Long id;

    /** 菜单编码 */
    private String menuCode;

    /** 菜单名称 */
    private String menuName;

    /** 菜单URL */
    private String url;

    /** 父菜单ID，顶级为0 */
    private Long parentId;

    /** 菜单类型：0-目录，1-菜单，2-按钮 */
    private String menuType;

    /** 图标 */
    private String icon;

    /** 排序号 */
    private Integer sort;

    /** 权限标识 */
    private String permission;

    /** 是否显示：1-是，0-否 */
    @TableField
    private BooleanEnum visible;

    /** 备注 */
    private String remark;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 创建者ID */
    private Long createId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者ID */
    private Long updateId;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
