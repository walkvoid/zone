package com.github.walkvoid.zone.user.model.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.github.walkvoid.zone.common.model.BaseDTO;
import com.github.walkvoid.zone.common.model.BooleanEnum;
import lombok.Data;

/**
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc
 */
@Data
public class RoleDTO extends BaseDTO {

    /**
     * 角色ID，主键
     */
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
