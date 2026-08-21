package com.github.walkvoid.zone.auth.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.walkvoid.zone.auth.db.entity.UserCredential;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCredentialMapper extends BaseMapper<UserCredential> {
}
