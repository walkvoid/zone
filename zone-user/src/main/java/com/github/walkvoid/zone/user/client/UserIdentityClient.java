package com.github.walkvoid.zone.user.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 调用 zone-auth 身份内部接口（定义在消费方，避免 auth↔user 循环依赖）。
 * identityType 传枚举名，例如 USERNAME / PHONE / EMAIL。
 */
@HttpExchange("/internal/identity")
public interface UserIdentityClient {

    @PostExchange
    void createIdentity(@RequestParam("userId") Long userId,
                        @RequestParam("identityType") String identityType,
                        @RequestParam("identifier") String identifier,
                        @RequestParam("verified") boolean verified);

    @GetExchange("/username/{username}/user-id")
    Long findUserIdByUsername(@PathVariable("username") String username);

    @GetExchange("/phone/{phone}/user-id")
    Long findUserIdByPhone(@PathVariable("phone") String phone);
}
