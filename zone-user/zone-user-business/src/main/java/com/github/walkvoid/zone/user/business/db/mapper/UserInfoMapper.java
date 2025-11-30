package com.github.walkvoid.zone.user.business.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息Mapper接口
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}
