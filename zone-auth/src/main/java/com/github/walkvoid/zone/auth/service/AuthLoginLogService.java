package com.github.walkvoid.zone.auth.service;

import com.github.walkvoid.zone.auth.model.enums.LoginTypeEnum;

public interface AuthLoginLogService {

    void logSuccess(Long userId, String username, LoginTypeEnum loginType, String clientIp, String userAgent);

    void logFailure(String username, LoginTypeEnum loginType, String failReason, String clientIp, String userAgent);
}
