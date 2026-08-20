package com.github.walkvoid.zone.user.api.client;

import com.github.walkvoid.zone.user.api.service.UserInfoService;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = ZoneUserServiceName.SERVICE_NAME, contextId = "userInfoFeignClient", path = "/internal/user-info")
public interface UserInfoFeignClient extends UserInfoService {

    @Override
    @GetMapping("/{id}")
    UserInfo getById(@PathVariable("id") Long id);

    @Override
    @GetMapping("/by-username/{username}")
    UserInfo getByUsername(@PathVariable("username") String username);

    @Override
    @PostMapping
    int insert(@RequestBody UserInfo entity);

    @Override
    @PutMapping
    int updateById(@RequestBody UserInfo entity);

    @Override
    @DeleteMapping("/{id}")
    int deleteById(@PathVariable("id") Long id);

    @Override
    @PostMapping("/delete-batch")
    int deleteBatchIds(@RequestBody List<Long> ids);

    @Override
    @PostMapping("/select-list")
    List<UserInfo> selectList(@RequestBody UserInfo condition);

    @Override
    @GetMapping("/exists/{username}")
    boolean checkUsernameExists(@PathVariable("username") String username);

    @Override
    @PutMapping("/{id}/last-login")
    int updateLastLoginInfo(@PathVariable("id") Long id,
                            @RequestParam("lastLoginTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastLoginTime,
                            @RequestParam("lastLoginIp") String lastLoginIp);

    @Override
    @PutMapping("/batch-status")
    int updateBatchStatus(@RequestParam("ids") List<Long> ids, @RequestParam("status") Integer status);
}
