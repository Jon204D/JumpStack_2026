package com.collabera.consolebankapp.dto;

import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;

public record AuthenticatedUserResponse(
        String username,
        Role role,
        String customerId) {

    public static AuthenticatedUserResponse from(UserCredential credential) {
        return new AuthenticatedUserResponse(
                credential.getUsername(),
                credential.getRole(),
                credential.getCustomerId());
    }
}
