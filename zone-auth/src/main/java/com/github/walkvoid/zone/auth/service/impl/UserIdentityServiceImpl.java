package com.github.walkvoid.zone.auth.service.impl;

import com.github.walkvoid.zone.auth.service.UserIdentityService;
import com.github.walkvoid.zone.auth.db.dao.UserIdentityDAO;
import com.github.walkvoid.zone.auth.db.entity.UserIdentity;
import com.github.walkvoid.zone.auth.model.enums.IdentityTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserIdentityServiceImpl implements UserIdentityService {

    @Autowired
    private UserIdentityDAO userIdentityDAO;

    @Override
    public void createIdentity(Long userId, IdentityTypeEnum identityType, String identifier, boolean verified) {
        if (userId == null || identityType == null || identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("identity parameters are required");
        }
        UserIdentity existing = userIdentityDAO.selectByTypeAndIdentifier(identityType, identifier.trim());
        if (existing != null) {
            return;
        }
        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setIdentityType(identityType);
        identity.setIdentifier(identifier.trim());
        identity.setVerified(verified ? 1 : 0);
        identity.setCreateTime(LocalDateTime.now());
        identity.setUpdateTime(LocalDateTime.now());
        userIdentityDAO.insert(identity);
    }

    @Override
    public Long findUserIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        UserIdentity identity = userIdentityDAO.selectByTypeAndIdentifier(IdentityTypeEnum.USERNAME, username.trim());
        return identity != null ? identity.getUserId() : null;
    }
}
