package com.github.walkvoid.zone.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.walkvoid.zone.common.model.BooleanEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
@TableName("role")
public class Role implements Serializable {
    private static final long serialVersionUID = -4763926818675481251L;

    @TableId
    private Long id;

    /** 角色编码 */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 角色描述 */
    private String description;

    /** 是否系统角色：0-否，1-是 */
    private BooleanEnum isSystem;

    /** 创建者ID */
    private Long createId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者ID */
    private Long updateId;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
