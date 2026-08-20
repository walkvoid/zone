package com.github.walkvoid.zone.auth.business.service.impl;

import com.github.walkvoid.zone.auth.api.service.UserCredentialService;
import com.github.walkvoid.zone.auth.business.db.dao.UserCredentialDAO;
import com.github.walkvoid.zone.auth.business.service.AuthSessionService;
import com.github.walkvoid.zone.auth.model.entity.UserCredential;
import com.github.walkvoid.zone.auth.model.enums.CredentialTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserCredentialServiceImpl implements UserCredentialService {

    @Autowired
    private UserCredentialDAO userCredentialDAO;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthSessionService authSessionService;

    @Override
    public void createPassword(Long userId, String rawPassword) {
        if (userId == null || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("userId and password are required");
        }
        UserCredential existing = userCredentialDAO.selectByUserIdAndType(userId, CredentialTypeEnum.PASSWORD);
        String hash = passwordEncoder.encode(rawPassword);
        if (existing != null) {
            userCredentialDAO.updateSecretHash(userId, CredentialTypeEnum.PASSWORD, hash);
            return;
        }
        UserCredential credential = new UserCredential();
        credential.setUserId(userId);
        credential.setCredentialType(CredentialTypeEnum.PASSWORD);
        credential.setSecretHash(hash);
        credential.setCreateTime(LocalDateTime.now());
        credential.setUpdateTime(LocalDateTime.now());
        userCredentialDAO.insert(credential);
    }

    @Override
    public boolean verifyPassword(Long userId, String rawPassword) {
        if (userId == null || rawPassword == null) {
            return false;
        }
        UserCredential credential = userCredentialDAO.selectByUserIdAndType(userId, CredentialTypeEnum.PASSWORD);
        if (credential == null || credential.getSecretHash() == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, credential.getSecretHash());
    }

    @Override
    public void updatePassword(Long userId, String rawPassword) {
        createPassword(userId, rawPassword);
        authSessionService.revokeAllByUserId(userId);
    }
}
