package com.github.walkvoid.zone.user.business.controller;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.user.business.db.dao.UserInfoDAO;
import com.github.walkvoid.zone.user.model.dto.UserInfoDTO;
import com.github.walkvoid.zone.user.model.entity.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户接口
 */
@Tag(name = "当前用户")
@RestController
@RequestMapping("/user")
public class CurrentUserController {

    @Autowired
    private UserInfoDAO userInfoDAO;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public ApiResult<UserInfoDTO> getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ApiResult.error(401, "未登录");
        }

        String username = auth.getName();
        UserInfo user = userInfoDAO.selectByUsername(username);
        if (user == null) {
            return ApiResult.error(401, "用户不存在");
        }

        UserInfoDTO dto = BeanCopyUtils.copyBean(user, UserInfoDTO.class);
        dto.setPassword(null);
        return ApiResult.ok(dto);
    }
}
