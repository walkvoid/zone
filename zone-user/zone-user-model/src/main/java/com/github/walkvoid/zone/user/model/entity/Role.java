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
 * 角色实体类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 角色表，存储系统角色信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {
    private static final long serialVersionUID = -4763926818675481251L;

    /**
     * 角色ID，主键
     */
    @TableId
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否系统角色：0-否，1-是
     */
    private BooleanEnum isSystem;

}
