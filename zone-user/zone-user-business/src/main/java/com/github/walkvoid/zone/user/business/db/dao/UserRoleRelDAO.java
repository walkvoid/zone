package com.github.walkvoid.zone.user.business.db.dao;

import com.github.walkvoid.zone.user.business.db.mapper.UserRoleRelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

/**
 * 用户角色关联DAO类
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc 用户角色关联数据访问层类，提供用户角色关联相关的数据库操作
 */
@Repository
public class UserRoleRelDAO {

    @Autowired
    private UserRoleRelMapper userRoleRelMapper;




    /**
     * 根据ID删除用户角色关联
     * @param id 关联ID
     * @return 删除成功的记录数
     */
    public int deleteById(Long id) {
        return userRoleRelMapper.deleteById(id);
    }

    /**
     * 批量删除用户角色关联
     * @param ids 关联ID列表
     * @return 删除成功的记录数
     */
    public int deleteBatchIds(List<Long> ids) {
        return userRoleRelMapper.deleteBatchIds(ids);
    }


}
