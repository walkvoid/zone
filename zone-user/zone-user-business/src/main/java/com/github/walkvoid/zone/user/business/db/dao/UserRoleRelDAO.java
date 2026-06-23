package com.github.walkvoid.zone.user.business.db.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.walkvoid.zone.user.business.db.mapper.UserRoleRelMapper;
import com.github.walkvoid.zone.user.model.entity.UserRoleRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联 DAO
 *
 * @author walkvoid
 */
@Repository
public class UserRoleRelDAO {

    @Autowired
    private UserRoleRelMapper userRoleRelMapper;

    public int insert(UserRoleRel rel) {
        return userRoleRelMapper.insert(rel);
    }

    public int deleteByUserIdAndRoleId(Long userId, Long roleId) {
        return userRoleRelMapper.delete(new QueryWrapper<UserRoleRel>()
                .eq("user_id", userId).eq("role_id", roleId));
    }

    public int deleteByUserId(Long userId) {
        return userRoleRelMapper.delete(new QueryWrapper<UserRoleRel>()
                .eq("user_id", userId));
    }

    public List<UserRoleRel> selectByUserId(Long userId) {
        return userRoleRelMapper.selectList(new QueryWrapper<UserRoleRel>()
                .eq("user_id", userId));
    }

    public List<UserRoleRel> selectByRoleId(Long roleId) {
        return userRoleRelMapper.selectList(new QueryWrapper<UserRoleRel>()
                .eq("role_id", roleId));
    }

    public int deleteById(Long id) {
        return userRoleRelMapper.deleteById(id);
    }

    public int deleteBatchIds(List<Long> ids) {
        return userRoleRelMapper.deleteBatchIds(ids);
    }
}
