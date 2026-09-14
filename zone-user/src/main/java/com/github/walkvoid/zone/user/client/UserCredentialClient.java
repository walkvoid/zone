package com.github.walkvoid.zone.user.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

/**
 * 调用 zone-auth 凭证内部接口（定义在消费方，避免 auth↔user 循环依赖）。
 */
@HttpExchange("/internal/credential")
public interface UserCredentialClient {

    @PostExchange("/password")
    void createPassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);

    @GetExchange("/verify")
    boolean verifyPassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);

    @PutExchange("/password")
    void updatePassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);
}
