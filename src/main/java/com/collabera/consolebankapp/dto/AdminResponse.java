package com.collabera.consolebankapp.dto;

import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;

public record AdminResponse(String id, String username, Role role) {

    public static AdminResponse from(UserCredential credential) {
        return new AdminResponse(
                credential.getId(),
                credential.getUsername(),
                credential.getRole());
    }
}
