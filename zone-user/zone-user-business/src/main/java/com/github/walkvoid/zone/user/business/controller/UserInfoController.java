package com.github.walkvoid.zone.user.business.controller;

import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.common.model.IdsParam;
import com.github.walkvoid.zone.common.model.PasswordParam;
import com.github.walkvoid.zone.user.business.db.dao.UserInfoDAO;
import com.github.walkvoid.zone.user.model.dto.UserInfoDTO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author walkvoid
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserInfoController {

    @Autowired
    private UserInfoDAO userInfoDAO;

    @Operation(summary = "新增用户")
    @PostMapping
    public UserInfoDTO add(@RequestBody UserInfoDTO dto) {
        if (Objects.isNull(dto)) return null;
        UserInfo entity = BeanCopyUtils.copyBean(dto, UserInfo.class);
        userInfoDAO.insert(entity);
        return BeanCopyUtils.copyBean(entity, UserInfoDTO.class);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public boolean delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        return !Objects.isNull(id) && userInfoDAO.deleteById(id) > 0;
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public boolean deleteBatch(@RequestBody IdsParam param) {
        if (Objects.isNull(param) || Objects.isNull(param.getIds()) || param.getIds().isEmpty()) return false;
        return userInfoDAO.deleteBatchIds(param.getIds()) > 0;
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public UserInfoDTO update(@RequestBody UserInfoDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getId())) return null;
        UserInfo entity = BeanCopyUtils.copyBean(dto, UserInfo.class);
        userInfoDAO.updateById(entity);
        return BeanCopyUtils.copyBean(userInfoDAO.selectById(dto.getId()), UserInfoDTO.class);
    }

    @Operation(summary = "查询用户")
    @GetMapping("/{id}")
    public UserInfoDTO getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        if (Objects.isNull(id)) return null;
        return BeanCopyUtils.copyBean(userInfoDAO.selectById(id), UserInfoDTO.class);
    }

    @Operation(summary = "条件查询用户列表")
    @GetMapping("/list")
    public List<UserInfoDTO> list(UserInfoDTO dto) {
        UserInfo condition = BeanCopyUtils.copyBean(dto, UserInfo.class);
        return userInfoDAO.selectList(condition).stream()
                .map(e -> BeanCopyUtils.copyBean(e, UserInfoDTO.class))
                .collect(Collectors.toList());
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public List<UserInfoDTO> page(@Parameter(description = "起始位置") @RequestParam(defaultValue = "0") int start,
                                  @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int limit,
                                  UserInfoDTO dto) {
        UserInfo condition = BeanCopyUtils.copyBean(dto, UserInfo.class);
        return userInfoDAO.selectPage(start, limit, condition).stream()
                .map(e -> BeanCopyUtils.copyBean(e, UserInfoDTO.class))
                .collect(Collectors.toList());
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public boolean toggleStatus(@Parameter(description = "用户ID") @PathVariable Long id,
                                @Parameter(description = "状态值") @RequestParam Integer status) {
        if (Objects.isNull(id) || Objects.isNull(status)) return false;
        return userInfoDAO.updateBatchStatus(List.of(id), status) > 0;
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    public boolean resetPassword(@Parameter(description = "用户ID") @PathVariable Long id,
                                  @RequestBody PasswordParam param) {
        if (Objects.isNull(id) || Objects.isNull(param)) return false;
        UserInfo entity = new UserInfo();
        entity.setId(id);
        entity.setPassword(param.getPassword());
        return userInfoDAO.updateById(entity) > 0;
    }

    @Operation(summary = "根据用户名查询")
    @GetMapping("/by-username/{username}")
    public UserInfoDTO getByUsername(@Parameter(description = "用户名") @PathVariable String username) {
        return BeanCopyUtils.copyBean(userInfoDAO.selectByUsername(username), UserInfoDTO.class);
    }
}
