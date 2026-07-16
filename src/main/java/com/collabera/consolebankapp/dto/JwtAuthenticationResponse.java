package com.collabera.consolebankapp.dto;

import com.collabera.consolebankapp.model.Role;

public record JwtAuthenticationResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username,
        Role role,
        String customerId) {
}
