package com.github.walkvoid.zone.user.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleAssignParam {

    private List<Long> roleIds;
}
