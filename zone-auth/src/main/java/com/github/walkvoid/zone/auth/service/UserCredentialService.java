package com.github.walkvoid.zone.auth.service;

/**
 * 用户凭证服务
 */
public interface UserCredentialService {

    /**
     * 创建密码凭证（明文密码在服务端加密）
     */
    void createPassword(Long userId, String rawPassword);

    /**
     * 校验密码
     */
    boolean verifyPassword(Long userId, String rawPassword);

    /**
     * 更新密码
     */
    void updatePassword(Long userId, String rawPassword);
}
