package com.github.walkvoid.zone.user.model.dto;

import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.zone.user.model.enums.UserGenderEnum;
import com.github.walkvoid.zone.user.model.enums.UserInfoStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserInfoDTO implements Serializable {

    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String nickname;
    private String avatar;
    private UserGenderEnum gender;
    private LocalDateTime birthday;
    private UserInfoStatusEnum status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private BooleanEnum isAdmin;
}
