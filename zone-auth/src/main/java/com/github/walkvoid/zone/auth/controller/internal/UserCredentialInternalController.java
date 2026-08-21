package com.github.walkvoid.zone.auth.controller.internal;

import com.github.walkvoid.zone.auth.service.UserCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/credential")
public class UserCredentialInternalController {

    @Autowired
    private UserCredentialService userCredentialService;

    @PostMapping("/password")
    public void createPassword(@RequestParam Long userId, @RequestParam String rawPassword) {
        userCredentialService.createPassword(userId, rawPassword);
    }

    @GetMapping("/verify")
    public boolean verifyPassword(@RequestParam Long userId, @RequestParam String rawPassword) {
        return userCredentialService.verifyPassword(userId, rawPassword);
    }

    @PutMapping("/password")
    public void updatePassword(@RequestParam Long userId, @RequestParam String rawPassword) {
        userCredentialService.updatePassword(userId, rawPassword);
    }
}
