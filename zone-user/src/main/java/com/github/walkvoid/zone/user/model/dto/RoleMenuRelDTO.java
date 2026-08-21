package com.github.walkvoid.zone.user.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleMenuRelDTO implements Serializable {

    private Long id;
    private Long roleId;
    private Long menuId;
}
