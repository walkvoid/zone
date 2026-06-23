package com.github.walkvoid.zone.user.business.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.walkvoid.zone.user.model.entity.RoleMenuRel;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-菜单关联 Mapper
 *
 * @author walkvoid
 */
public interface RoleMenuRelMapper extends BaseMapper<RoleMenuRel> {

    @Select("SELECT menu_id FROM role_menu_rel WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(Long roleId);
}
