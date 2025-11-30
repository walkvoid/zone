package com.github.walkvoid.zone.user.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.user.business.db.mapper.UserInfoMapper;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 用户信息DAO类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Repository
public class UserInfoDao {

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    public UserInfo getUserInfoById(Long id) {
        return userInfoMapper.selectById(id);
    }

    /**
     * 根据手机号查询用户信息（用于手机号登录）
     * @param phone 手机号
     * @return 用户信息
     */
    public UserInfo getUserInfoByPhone(String phone) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 根据邮箱查询用户信息（用于邮箱登录）
     * @param email 邮箱
     * @return 用户信息
     */
    public UserInfo getUserInfoByEmail(String email) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    public UserInfo getUserInfoByUsername(String username) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 保存用户信息
     * @param userInfo 用户信息
     * @return 影响行数
     */
    public int saveUserInfo(UserInfo userInfo) {
        return userInfoMapper.insert(userInfo);
    }

    /**
     * 更新用户信息
     * @param userInfo 用户信息
     * @return 影响行数
     */
    public int updateUserInfo(UserInfo userInfo) {
        return userInfoMapper.updateById(userInfo);
    }

    /**
     * 更新用户最后登录信息
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     * @param lastLoginIp 最后登录IP
     * @return 影响行数
     */
    public int updateLastLoginInfo(Long id, LocalDateTime lastLoginTime, String lastLoginIp) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setLastLoginTime(lastLoginTime);
        userInfo.setLastLoginIp(lastLoginIp);
        return userInfoMapper.updateById(userInfo);
    }

    /**
     * 检查手机号是否已存在
     * @param phone 手机号
     * @return 是否存在
     */
    public boolean checkPhoneExists(String phone) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return userInfoMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    public boolean checkEmailExists(String email) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userInfoMapper.selectCount(queryWrapper) > 0;
    }
}
