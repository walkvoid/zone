package com.github.walkvoid.zone.auth.api.client;

import com.github.walkvoid.zone.auth.api.service.UserCredentialService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = ZoneAuthServiceName.SERVICE_NAME, contextId = "userCredentialFeignClient", path = "/internal/credential")
public interface UserCredentialFeignClient extends UserCredentialService {

    @Override
    @PostMapping("/password")
    void createPassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);

    @Override
    @GetMapping("/verify")
    boolean verifyPassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);

    @Override
    @PutMapping("/password")
    void updatePassword(@RequestParam("userId") Long userId, @RequestParam("rawPassword") String rawPassword);
}
