package com.github.walkvoid.zone.user.business.controller;

import com.github.walkvoid.wvframework.models.PageRequest;
import com.github.walkvoid.wvframework.models.PageResponse;
import com.github.walkvoid.wvframework.models.WebPageResponse;
import com.github.walkvoid.wvframework.models.WebResponse;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.user.model.dto.IdsParam;
import com.github.walkvoid.zone.user.model.dto.PasswordParam;
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
    public WebResponse<UserInfoDTO> add(@RequestBody UserInfoDTO dto) {
        if (Objects.isNull(dto)) return WebResponse.ok(null);
        UserInfo entity = BeanCopyUtils.copyBean(dto, UserInfo.class);
        userInfoDAO.insert(entity);
        return WebResponse.ok(BeanCopyUtils.copyBean(entity, UserInfoDTO.class));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public WebResponse<Boolean> delete(@Parameter(description = "用户ID") @PathVariable("id") Long id) {
        return WebResponse.ok(!Objects.isNull(id) && userInfoDAO.deleteById(id) > 0);
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public WebResponse<Boolean> deleteBatch(@RequestBody IdsParam param) {
        if (Objects.isNull(param) || Objects.isNull(param.getIds()) || param.getIds().isEmpty()) return WebResponse.ok(false);
        return WebResponse.ok(userInfoDAO.deleteBatchIds(param.getIds()) > 0);
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public WebResponse<UserInfoDTO> update(@RequestBody UserInfoDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getId())) return WebResponse.ok(null);
        UserInfo entity = BeanCopyUtils.copyBean(dto, UserInfo.class);
        userInfoDAO.updateById(entity);
        return WebResponse.ok(BeanCopyUtils.copyBean(userInfoDAO.selectById(dto.getId()), UserInfoDTO.class));
    }

    @Operation(summary = "查询用户")
    @GetMapping("/{id}")
    public WebResponse<UserInfoDTO> getById(@Parameter(description = "用户ID") @PathVariable("id") Long id) {
        if (Objects.isNull(id)) return WebResponse.ok(null);
        return WebResponse.ok(BeanCopyUtils.copyBean(userInfoDAO.selectById(id), UserInfoDTO.class));
    }

    @Operation(summary = "条件查询用户列表")
    @GetMapping("/list")
    public WebResponse<List<UserInfoDTO>> list(UserInfoDTO dto) {
        UserInfo condition = BeanCopyUtils.copyBean(dto, UserInfo.class);
        return WebResponse.ok(userInfoDAO.selectList(condition).stream()
                .map(e -> BeanCopyUtils.copyBean(e, UserInfoDTO.class))
                .collect(Collectors.toList()));
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public WebPageResponse<UserInfoDTO> page(
            @RequestParam(value = "current", defaultValue = "0") long current,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute UserInfoDTO parameter) {
        PageRequest<UserInfoDTO> pageRequest = PageRequest.of(current, size, parameter);
        PageResponse<UserInfoDTO> pageResponse = userInfoDAO.page(pageRequest);
        return WebPageResponse.ok(pageResponse);
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public WebResponse<Boolean> toggleStatus(@Parameter(description = "用户ID") @PathVariable("id") Long id,
                                @Parameter(description = "状态值") @RequestParam Integer status) {
        if (Objects.isNull(id) || Objects.isNull(status)) return WebResponse.ok(false);
        return WebResponse.ok(userInfoDAO.updateBatchStatus(List.of(id), status) > 0);
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    public WebResponse<Boolean> resetPassword(@Parameter(description = "用户ID") @PathVariable("id") Long id,
                                  @RequestBody PasswordParam param) {
        if (Objects.isNull(id) || Objects.isNull(param)) return WebResponse.ok(false);
        UserInfo entity = new UserInfo();
        entity.setId(id);
        entity.setPassword(param.getPassword());
        return WebResponse.ok(userInfoDAO.updateById(entity) > 0);
    }

    @Operation(summary = "根据用户名查询")
    @GetMapping("/by-username/{username}")
    public WebResponse<UserInfoDTO> getByUsername(@Parameter(description = "用户名") @PathVariable("username") String username) {
        return WebResponse.ok(BeanCopyUtils.copyBean(userInfoDAO.selectByUsername(username), UserInfoDTO.class));
    }
}
