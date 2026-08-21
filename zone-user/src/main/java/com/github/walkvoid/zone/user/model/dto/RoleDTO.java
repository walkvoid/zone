package com.github.walkvoid.zone.user.model.dto;

import com.github.walkvoid.wvframework.models.BooleanEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RoleDTO implements Serializable {

    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private BooleanEnum isSystem;
}
