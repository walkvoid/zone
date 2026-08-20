package com.github.walkvoid.zone.auth.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.walkvoid.zone.auth.business.db.mapper.AuthRefreshTokenMapper;
import com.github.walkvoid.zone.auth.model.entity.AuthRefreshToken;
import com.github.walkvoid.zone.auth.model.enums.SessionStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class AuthRefreshTokenDAO {

    @Autowired
    private AuthRefreshTokenMapper authRefreshTokenMapper;

    public AuthRefreshToken selectActiveByTokenHash(String tokenHash) {
        QueryWrapper<AuthRefreshToken> wrapper = new QueryWrapper<>();
        wrapper.eq("token_hash", tokenHash)
                .eq("status", SessionStatusEnum.ACTIVE.name())
                .gt("expires_at", LocalDateTime.now());
        return authRefreshTokenMapper.selectOne(wrapper);
    }

    public int insert(AuthRefreshToken session) {
        return authRefreshTokenMapper.insert(session);
    }

    public int updateStatus(Long id, SessionStatusEnum status) {
        AuthRefreshToken entity = new AuthRefreshToken();
        entity.setStatus(status);
        entity.setUpdateTime(LocalDateTime.now());
        UpdateWrapper<AuthRefreshToken> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        return authRefreshTokenMapper.update(entity, wrapper);
    }

    public int revokeByTokenHash(String tokenHash) {
        AuthRefreshToken entity = new AuthRefreshToken();
        entity.setStatus(SessionStatusEnum.REVOKED);
        entity.setUpdateTime(LocalDateTime.now());
        UpdateWrapper<AuthRefreshToken> wrapper = new UpdateWrapper<>();
        wrapper.eq("token_hash", tokenHash).eq("status", SessionStatusEnum.ACTIVE.name());
        return authRefreshTokenMapper.update(entity, wrapper);
    }

    public int revokeAllActiveByUserId(Long userId) {
        AuthRefreshToken entity = new AuthRefreshToken();
        entity.setStatus(SessionStatusEnum.REVOKED);
        entity.setUpdateTime(LocalDateTime.now());
        UpdateWrapper<AuthRefreshToken> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId).eq("status", SessionStatusEnum.ACTIVE.name());
        return authRefreshTokenMapper.update(entity, wrapper);
    }
}
