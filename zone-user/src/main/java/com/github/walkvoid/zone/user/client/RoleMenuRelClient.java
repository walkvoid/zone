package com.github.walkvoid.zone.user.client;

import com.github.walkvoid.zone.user.db.entity.RoleMenuRel;
import com.github.walkvoid.zone.user.service.RoleMenuRelService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/internal/role-menu-rel")
public interface RoleMenuRelClient extends RoleMenuRelService {

    @Override
    @PostExchange
    int insert(@RequestBody RoleMenuRel rel);

    @Override
    @DeleteExchange("/by-role/{roleId}")
    int deleteByRoleId(@PathVariable("roleId") Long roleId);

    @Override
    @DeleteExchange("/by-menu/{menuId}")
    int deleteByMenuId(@PathVariable("menuId") Long menuId);

    @Override
    @DeleteExchange("/by-role/{roleId}/menu/{menuId}")
    int deleteByRoleIdAndMenuId(@PathVariable("roleId") Long roleId, @PathVariable("menuId") Long menuId);

    @Override
    @GetExchange("/menu-ids/{roleId}")
    List<Long> selectMenuIdsByRoleId(@PathVariable("roleId") Long roleId);

    @Override
    @GetExchange("/by-role/{roleId}")
    List<RoleMenuRel> selectByRoleId(@PathVariable("roleId") Long roleId);
}
