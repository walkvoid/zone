package com.github.walkvoid.zone.auth.api.service;

import com.github.walkvoid.zone.auth.model.enums.IdentityTypeEnum;

/**
 * 用户登录身份服务
 */
public interface UserIdentityService {

    void createIdentity(Long userId, IdentityTypeEnum identityType, String identifier, boolean verified);

    /**
     * 按用户名查找 userId，不存在返回 null
     */
    Long findUserIdByUsername(String username);
}
