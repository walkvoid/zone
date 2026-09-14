package com.github.walkvoid.zone.user.client;

import com.github.walkvoid.zone.user.db.entity.Role;
import com.github.walkvoid.zone.user.service.RoleService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange("/internal/role")
public interface RoleClient extends RoleService {

    @Override
    @GetExchange("/{id}")
    Role getById(@PathVariable("id") Long id);

    @Override
    @GetExchange("/all")
    List<Role> selectAll();

    @Override
    @PostExchange("/select-list")
    List<Role> selectList(@RequestBody Role condition);

    @Override
    @GetExchange("/codes/{userId}")
    List<String> getRoleCodesByUserId(@PathVariable("userId") Long userId);
}
