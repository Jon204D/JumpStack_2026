package com.collabera.consolebankapp.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collabera.consolebankapp.dto.AuthenticatedUserResponse;
import com.collabera.consolebankapp.dto.JwtAuthenticationResponse;
import com.collabera.consolebankapp.dto.LoginRequest;
import com.collabera.consolebankapp.exception.ForbiddenOperationException;
import com.collabera.consolebankapp.exception.InvalidCredentialsException;
import com.collabera.consolebankapp.repository.UserCredentialRepository;
import com.collabera.consolebankapp.security.JwtTokenService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserCredentialRepository userCredentialRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthController(UserCredentialRepository userCredentialRepository,
            AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.userCredentialRepository = userCredentialRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public JwtAuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(), request.password()));
            return userCredentialRepository.findByUsernameIgnoreCase(authentication.getName())
                    .map(credential -> jwtTokenService.issueToken(authentication, credential))
                    .orElseThrow(() -> new ForbiddenOperationException(
                            "Authenticated user is not linked to a bank user"));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
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
