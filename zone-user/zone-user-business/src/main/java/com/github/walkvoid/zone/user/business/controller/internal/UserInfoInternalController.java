package com.github.walkvoid.zone.user.business.controller.internal;

import com.github.walkvoid.zone.user.api.service.UserInfoService;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息服务内部 API（供 Feign 调用）
 */
@RestController
@RequestMapping("/internal/user-info")
public class UserInfoInternalController {

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/{id}")
    public UserInfo getById(@PathVariable Long id) {
        return userInfoService.getById(id);
    }

    @GetMapping("/by-username/{username}")
    public UserInfo getByUsername(@PathVariable String username) {
        return userInfoService.getByUsername(username);
    }

    @PostMapping
    public int insert(@RequestBody UserInfo entity) {
        return userInfoService.insert(entity);
    }

    @PutMapping
    public int updateById(@RequestBody UserInfo entity) {
        return userInfoService.updateById(entity);
    }

    @DeleteMapping("/{id}")
    public int deleteById(@PathVariable Long id) {
        return userInfoService.deleteById(id);
    }

    @PostMapping("/delete-batch")
    public int deleteBatchIds(@RequestBody List<Long> ids) {
        return userInfoService.deleteBatchIds(ids);
    }

    @PostMapping("/select-list")
    public List<UserInfo> selectList(@RequestBody UserInfo condition) {
        return userInfoService.selectList(condition);
    }

    @GetMapping("/exists/{username}")
    public boolean checkUsernameExists(@PathVariable String username) {
        return userInfoService.checkUsernameExists(username);
    }

    @PutMapping("/{id}/last-login")
    public int updateLastLoginInfo(@PathVariable Long id,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastLoginTime,
                                   @RequestParam String lastLoginIp) {
        return userInfoService.updateLastLoginInfo(id, lastLoginTime, lastLoginIp);
    }

    @PutMapping("/batch-status")
    public int updateBatchStatus(@RequestParam List<Long> ids, @RequestParam Integer status) {
        return userInfoService.updateBatchStatus(ids, status);
    }
}
