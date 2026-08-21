package com.github.walkvoid.zone.auth.db.dao;

import com.github.walkvoid.zone.auth.db.mapper.AuthLoginLogMapper;
import com.github.walkvoid.zone.auth.db.entity.AuthLoginLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AuthLoginLogDAO {

    @Autowired
    private AuthLoginLogMapper authLoginLogMapper;

    public int insert(AuthLoginLog log) {
        return authLoginLogMapper.insert(log);
    }
}
