package com.github.walkvoid.zone.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户角色关联实体
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
@TableName("user_role_rel")
public class UserRoleRel implements Serializable {
    private static final long serialVersionUID = -1573689426386712548L;

    @TableId
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;
}
