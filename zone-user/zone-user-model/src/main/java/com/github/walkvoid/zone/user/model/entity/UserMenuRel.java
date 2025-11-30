package com.github.walkvoid.zone.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户菜单角色关联实体类（视图或DTO）
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 用于联合查询用户、菜单和角色的关联信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_menu_rel")
public class UserMenuRel extends BaseEntity {
    private static final long serialVersionUID = 8736542910987654321L;

    /**
     * ID，主键
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 菜单
     */
    private Long menuId;
}
