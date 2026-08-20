package com.github.walkvoid.zone.user.business.service.impl;

import com.github.walkvoid.zone.user.api.service.UserInfoService;
import com.github.walkvoid.zone.user.business.db.dao.UserInfoDAO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoDAO userInfoDAO;

    @Override
    public UserInfo getById(Long id) {
        return userInfoDAO.selectById(id);
    }

    @Override
    public UserInfo getByUsername(String username) {
        return userInfoDAO.selectByUsername(username);
    }

    @Override
    public int insert(UserInfo entity) {
        return userInfoDAO.insert(entity);
    }

    @Override
    public int updateById(UserInfo entity) {
        return userInfoDAO.updateById(entity);
    }

    @Override
    public int deleteById(Long id) {
        return userInfoDAO.deleteById(id);
    }

    @Override
    public int deleteBatchIds(List<Long> ids) {
        return userInfoDAO.deleteBatchIds(ids);
    }

    @Override
    public List<UserInfo> selectList(UserInfo condition) {
        return userInfoDAO.selectList(condition);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userInfoDAO.checkUsernameExists(username) > 0;
    }

    @Override
    public int updateLastLoginInfo(Long id, LocalDateTime lastLoginTime, String lastLoginIp) {
        return userInfoDAO.updateLastLoginInfo(id, lastLoginTime, lastLoginIp);
    }

    @Override
    public int updateBatchStatus(List<Long> ids, Integer status) {
        return userInfoDAO.updateBatchStatus(ids, status);
    }
}
