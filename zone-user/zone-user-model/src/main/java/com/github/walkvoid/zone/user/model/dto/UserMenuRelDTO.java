package com.github.walkvoid.zone.user.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc
 */
@Data
public class UserMenuRelDTO extends UserInfoDTO {

    private List<MenuDTO> menuList;
}
