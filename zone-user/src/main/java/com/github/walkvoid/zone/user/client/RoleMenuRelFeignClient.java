package com.github.walkvoid.zone.user.client;

import com.github.walkvoid.zone.user.service.RoleMenuRelService;
import com.github.walkvoid.zone.user.db.entity.RoleMenuRel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = ZoneUserServiceName.SERVICE_NAME, contextId = "roleMenuRelFeignClient", path = "/internal/role-menu-rel")
public interface RoleMenuRelFeignClient extends RoleMenuRelService {

    @Override
    @PostMapping
    int insert(@RequestBody RoleMenuRel rel);

    @Override
    @DeleteMapping("/by-role/{roleId}")
    int deleteByRoleId(@PathVariable("roleId") Long roleId);

    @Override
    @DeleteMapping("/by-menu/{menuId}")
    int deleteByMenuId(@PathVariable("menuId") Long menuId);

    @Override
    @DeleteMapping("/by-role/{roleId}/menu/{menuId}")
    int deleteByRoleIdAndMenuId(@PathVariable("roleId") Long roleId, @PathVariable("menuId") Long menuId);

    @Override
    @GetMapping("/menu-ids/{roleId}")
    List<Long> selectMenuIdsByRoleId(@PathVariable("roleId") Long roleId);

    @Override
    @GetMapping("/by-role/{roleId}")
    List<RoleMenuRel> selectByRoleId(@PathVariable("roleId") Long roleId);
}
