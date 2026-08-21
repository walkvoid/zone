package com.github.walkvoid.zone.user.service;

import com.github.walkvoid.zone.user.db.entity.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息服务接口
 *
 * @author walkvoid
 */
public interface UserInfoService {

    UserInfo getById(Long id);

    UserInfo getByUsername(String username);

    int insert(UserInfo entity);

    int updateById(UserInfo entity);

    int deleteById(Long id);

    int deleteBatchIds(List<Long> ids);

    List<UserInfo> selectList(UserInfo condition);

    boolean checkUsernameExists(String username);

    int updateLastLoginInfo(Long id, LocalDateTime lastLoginTime, String lastLoginIp);

    int updateBatchStatus(List<Long> ids, Integer status);
}
