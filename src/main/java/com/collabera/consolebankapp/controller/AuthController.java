package com.collabera.consolebankapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collabera.consolebankapp.dto.AuthenticatedUserResponse;
import com.collabera.consolebankapp.exception.ForbiddenOperationException;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserCredentialRepository userCredentialRepository;

    public AuthController(UserCredentialRepository userCredentialRepository) {
        this.userCredentialRepository = userCredentialRepository;
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenOperationException("Authentication is required");
        }

        return userCredentialRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(AuthenticatedUserResponse::from)
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated user is not linked to a bank user"));
    }
}
