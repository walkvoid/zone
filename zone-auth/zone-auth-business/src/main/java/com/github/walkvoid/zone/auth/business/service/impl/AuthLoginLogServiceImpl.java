package com.github.walkvoid.zone.auth.business.service.impl;

import com.github.walkvoid.zone.auth.business.db.dao.AuthLoginLogDAO;
import com.github.walkvoid.zone.auth.business.service.AuthLoginLogService;
import com.github.walkvoid.zone.auth.model.entity.AuthLoginLog;
import com.github.walkvoid.zone.auth.model.enums.LoginTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthLoginLogServiceImpl implements AuthLoginLogService {

    @Autowired
    private AuthLoginLogDAO authLoginLogDAO;

    @Override
    public void logSuccess(Long userId, String username, LoginTypeEnum loginType,
                           String clientIp, String userAgent) {
        AuthLoginLog log = baseLog(username, loginType, clientIp, userAgent);
        log.setUserId(userId);
        log.setSuccess(1);
        authLoginLogDAO.insert(log);
    }

    @Override
    public void logFailure(String username, LoginTypeEnum loginType, String failReason,
                           String clientIp, String userAgent) {
        AuthLoginLog log = baseLog(username, loginType, clientIp, userAgent);
        log.setSuccess(0);
        log.setFailReason(failReason);
        authLoginLogDAO.insert(log);
    }

    private AuthLoginLog baseLog(String username, LoginTypeEnum loginType,
                                 String clientIp, String userAgent) {
        AuthLoginLog log = new AuthLoginLog();
        log.setUsername(username);
        log.setLoginType(loginType);
        log.setClientIp(clientIp);
        log.setUserAgent(truncateUserAgent(userAgent));
        log.setCreateTime(LocalDateTime.now());
        return log;
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() > 512 ? userAgent.substring(0, 512) : userAgent;
    }
}
