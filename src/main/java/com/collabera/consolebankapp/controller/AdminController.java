package com.collabera.consolebankapp.controller;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    @GetMapping("/admin")
    public Map<String, String> admin(Authentication authentication) {
        return Map.of("message", "Admin access granted", "username", authentication.getName());
    }

    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        return Map.of("message", "Public endpoint is available");
    }
}
