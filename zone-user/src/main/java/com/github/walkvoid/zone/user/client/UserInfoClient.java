package com.github.walkvoid.zone.user.client;

import com.github.walkvoid.zone.user.db.entity.UserInfo;
import com.github.walkvoid.zone.user.service.UserInfoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.time.LocalDateTime;
import java.util.List;

@HttpExchange("/internal/user-info")
public interface UserInfoClient extends UserInfoService {

    @Override
    @GetExchange("/{id}")
    UserInfo getById(@PathVariable("id") Long id);

    @Override
    @GetExchange("/by-username/{username}")
    UserInfo getByUsername(@PathVariable("username") String username);

    @Override
    @PostExchange
    int insert(@RequestBody UserInfo entity);

    @Override
    @PutExchange
    int updateById(@RequestBody UserInfo entity);

    @Override
    @DeleteExchange("/{id}")
    int deleteById(@PathVariable("id") Long id);

    @Override
    @PostExchange("/delete-batch")
    int deleteBatchIds(@RequestBody List<Long> ids);

    @Override
    @PostExchange("/select-list")
    List<UserInfo> selectList(@RequestBody UserInfo condition);

    @Override
    @GetExchange("/exists/{username}")
    boolean checkUsernameExists(@PathVariable("username") String username);

    @Override
    @PutExchange("/{id}/last-login")
    int updateLastLoginInfo(@PathVariable("id") Long id,
                            @RequestParam("lastLoginTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastLoginTime,
                            @RequestParam("lastLoginIp") String lastLoginIp);

    @Override
    @PutExchange("/batch-status")
    int updateBatchStatus(@RequestParam("ids") List<Long> ids, @RequestParam("status") Integer status);
}
