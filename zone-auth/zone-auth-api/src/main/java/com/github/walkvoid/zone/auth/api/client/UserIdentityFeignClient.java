package com.github.walkvoid.zone.auth.api.client;

import com.github.walkvoid.zone.auth.api.service.UserIdentityService;
import com.github.walkvoid.zone.auth.model.enums.IdentityTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = ZoneAuthServiceName.SERVICE_NAME, contextId = "userIdentityFeignClient", path = "/internal/identity")
public interface UserIdentityFeignClient extends UserIdentityService {

    @Override
    @PostMapping
    void createIdentity(@RequestParam("userId") Long userId,
                        @RequestParam("identityType") IdentityTypeEnum identityType,
                        @RequestParam("identifier") String identifier,
                        @RequestParam("verified") boolean verified);

    @Override
    @GetMapping("/username/{username}/user-id")
    Long findUserIdByUsername(@PathVariable("username") String username);
}
