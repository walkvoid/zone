package com.github.walkvoid.zone.auth.controller.internal;

import com.github.walkvoid.zone.auth.service.UserIdentityService;
import com.github.walkvoid.zone.auth.model.enums.IdentityTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/identity")
public class UserIdentityInternalController {

    @Autowired
    private UserIdentityService userIdentityService;

    @PostMapping
    public void createIdentity(@RequestParam Long userId,
                               @RequestParam IdentityTypeEnum identityType,
                               @RequestParam String identifier,
                               @RequestParam boolean verified) {
        userIdentityService.createIdentity(userId, identityType, identifier, verified);
    }

    @GetMapping("/username/{username}/user-id")
    public Long findUserIdByUsername(@PathVariable String username) {
        return userIdentityService.findUserIdByUsername(username);
    }
}
