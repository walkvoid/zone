package com.github.walkvoid.zone.auth.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.auth.db.mapper.UserCredentialMapper;
import com.github.walkvoid.zone.auth.db.entity.UserCredential;
import com.github.walkvoid.zone.auth.model.enums.CredentialTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class UserCredentialDAO {

    @Autowired
    private UserCredentialMapper userCredentialMapper;

    public UserCredential selectByUserIdAndType(Long userId, CredentialTypeEnum type) {
        QueryWrapper<UserCredential> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("credential_type", type.name());
        return userCredentialMapper.selectOne(wrapper);
    }

    public int insert(UserCredential credential) {
        return userCredentialMapper.insert(credential);
    }

    public int updateSecretHash(Long userId, CredentialTypeEnum type, String secretHash) {
        UserCredential entity = new UserCredential();
        entity.setSecretHash(secretHash);
        entity.setUpdateTime(LocalDateTime.now());
        QueryWrapper<UserCredential> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("credential_type", type.name());
        return userCredentialMapper.update(entity, wrapper);
    }
}
