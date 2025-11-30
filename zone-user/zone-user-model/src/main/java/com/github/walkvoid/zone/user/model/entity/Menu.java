package com.github.walkvoid.zone.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.common.model.BaseEntity;
import com.github.walkvoid.zone.common.model.BooleanEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 菜单实体类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 菜单表，存储系统菜单信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("menu")
public class Menu extends BaseEntity {
    private static final long serialVersionUID = -8527693140486687132L;

    /**
     * 菜单ID，主键
     */
    @TableId
    private Long id;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单URL
     */
    private String url;

    /**
     * 父菜单ID，顶级菜单为0
     */
    private Long parentId;

    /**
     * 菜单类型：0-目录，1-菜单，2-按钮
     */
    private String menuType;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 是否显示：0-不显示，1-显示
     */
    private BooleanEnum visible;


    /**
     * 备注
     */
    private String remark;
}
