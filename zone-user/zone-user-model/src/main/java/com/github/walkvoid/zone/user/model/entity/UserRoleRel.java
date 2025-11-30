package com.github.walkvoid.zone.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 用户和角色关联表，存储用户与角色的多对多关系
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_role_rel")
public class UserRoleRel extends BaseEntity {
    private static final long serialVersionUID = -1573689426386712548L;

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
     * 角色ID
     */
    private Long roleId;

}
