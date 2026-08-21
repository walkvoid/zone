package com.github.walkvoid.zone.auth.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.auth.db.mapper.UserIdentityMapper;
import com.github.walkvoid.zone.auth.db.entity.UserIdentity;
import com.github.walkvoid.zone.auth.model.enums.IdentityTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserIdentityDAO {

    @Autowired
    private UserIdentityMapper userIdentityMapper;

    public UserIdentity selectByTypeAndIdentifier(IdentityTypeEnum type, String identifier) {
        QueryWrapper<UserIdentity> wrapper = new QueryWrapper<>();
        wrapper.eq("identity_type", type.name()).eq("identifier", identifier);
        return userIdentityMapper.selectOne(wrapper);
    }

    public int insert(UserIdentity identity) {
        return userIdentityMapper.insert(identity);
    }
}
