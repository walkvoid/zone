package com.github.walkvoid.zone.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用 zone-auth 凭证内部接口（定义在消费方，避免 auth↔user 循环依赖）。
 */
@FeignClient(name = ZoneAuthServiceName.SERVICE_NAME, contextId = "userCredentialFeignClient", path = "/internal/credential")
public interface UserCredentialFeignClient {

    @PostMapping("/password")
    void createPassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);

    @GetMapping("/verify")
    boolean verifyPassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);

    @PutMapping("/password")
    void updatePassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);
}
