package com.github.walkvoid.zone.user.business.db.dao;

import com.github.walkvoid.zone.user.business.db.mapper.UserInfoMapper;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息DAO类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 用户信息数据访问层类，提供用户相关的数据库操作
 */
@Repository
public class UserInfoDAO {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    public UserInfo selectById(Long id) {
        return userInfoMapper.selectById(id);
    }

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户信息
     */
    public UserInfo selectByUsername(String username) {
        // 使用条件构造器查询
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 根据手机号查询用户信息
     * @param phone 手机号
     * @return 用户信息
     */
    public UserInfo selectByPhone(String phone) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 根据邮箱查询用户信息
     * @param email 邮箱
     * @return 用户信息
     */
    public UserInfo selectByEmail(String email) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 插入用户信息
     * @param userInfo 用户信息
     * @return 影响行数
     */
    public int insert(UserInfo userInfo) {
        return userInfoMapper.insert(userInfo);
    }

    /**
     * 更新用户信息
     * @param userInfo 用户信息
     * @return 影响行数
     */
    public int updateById(UserInfo userInfo) {
        return userInfoMapper.updateById(userInfo);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    public int deleteById(Long id) {
        return userInfoMapper.deleteById(id);
    }

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 影响行数
     */
    public int deleteBatchIds(List<Long> ids) {
        return userInfoMapper.deleteBatchIds(ids);
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
     * 查询所有用户
     * @return 用户列表
     */
    public List<UserInfo> selectAll() {
        return userInfoMapper.selectList(null);
    }

    /**
     * 根据条件查询用户列表
     * @param userInfo 查询条件
     * @return 用户列表
     */
    public List<UserInfo> selectList(UserInfo userInfo) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(userInfo);
        return userInfoMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询用户列表
     * @param start 起始位置
     * @param limit 每页数量
     * @param userInfo 查询条件
     * @return 用户列表
     */
    public List<UserInfo> selectPage(int start, int limit, UserInfo userInfo) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(userInfo);
        return userInfoMapper.selectList(queryWrapper);
    }

    /**
     * 统计用户数量
     * @return 用户数量
     */
    public long count() {
        return userInfoMapper.selectCount(null);
    }

    /**
     * 根据条件统计用户数量
     * @param userInfo 查询条件
     * @return 用户数量
     */
    public long countByCondition(UserInfo userInfo) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>(userInfo);
        return userInfoMapper.selectCount(queryWrapper);
    }

    /**
     * 检查手机号是否已存在
     * @param phone 手机号
     * @return 存在数量
     */
    public int checkPhoneExists(String phone) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return Math.toIntExact(userInfoMapper.selectCount(queryWrapper));
    }

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 存在数量
     */
    public int checkEmailExists(String email) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("email", email);
        return Math.toIntExact(userInfoMapper.selectCount(queryWrapper));
    }

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 存在数量
     */
    public int checkUsernameExists(String username) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("username", username);
        return Math.toIntExact(userInfoMapper.selectCount(queryWrapper));
    }

    /**
     * 根据角色ID查询用户列表
     * @param roleId 角色ID
     * @return 用户列表
     */
    public List<UserInfo> selectUsersByRoleId(Long roleId) {
        // 此方法可能需要在UserInfoMapper中自定义SQL实现
        // 这里先用空列表返回，后续可以扩展
        return null;
    }

    /**
     * 查询启用的用户
     * @return 用户列表
     */
    public List<UserInfo> selectEnabledUsers() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("status", 1); // 假设1表示启用状态
        return userInfoMapper.selectList(queryWrapper);
    }

    /**
     * 批量更新用户状态
     * @param ids 用户ID列表
     * @param status 状态
     * @return 影响行数
     */
    public int updateBatchStatus(List<Long> ids, Integer status) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UserInfo> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.in("id", ids).set("status", status);
        UserInfo userInfo = new UserInfo();
        return userInfoMapper.update(userInfo, updateWrapper);
    }
}
