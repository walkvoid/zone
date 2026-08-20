package com.github.walkvoid.zone.user.api.client;

import com.github.walkvoid.zone.user.api.service.RoleService;
import com.github.walkvoid.zone.user.model.entity.Role;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = ZoneUserServiceName.SERVICE_NAME, contextId = "roleFeignClient", path = "/internal/role")
public interface RoleFeignClient extends RoleService {

    @Override
    @GetMapping("/{id}")
    Role getById(@PathVariable("id") Long id);

    @Override
    @GetMapping("/all")
    List<Role> selectAll();

    @Override
    @PostMapping("/select-list")
    List<Role> selectList(@RequestBody Role condition);

    @Override
    @GetMapping("/codes/{userId}")
    List<String> getRoleCodesByUserId(@PathVariable("userId") Long userId);
}
