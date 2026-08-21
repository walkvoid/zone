package com.github.walkvoid.zone.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用 zone-auth 身份内部接口（定义在消费方，避免 auth↔user 循环依赖）。
 * identityType 传枚举名，例如 USERNAME / PHONE / EMAIL。
 */
@FeignClient(name = ZoneAuthServiceName.SERVICE_NAME, contextId = "userIdentityFeignClient", path = "/internal/identity")
public interface UserIdentityFeignClient {

    @PostMapping
    void createIdentity(@RequestParam("userId") Long userId,
                        @RequestParam("identityType") String identityType,
                        @RequestParam("identifier") String identifier,
                        @RequestParam("verified") boolean verified);

    @GetMapping("/username/{username}/user-id")
    Long findUserIdByUsername(@PathVariable("username") String username);
}
