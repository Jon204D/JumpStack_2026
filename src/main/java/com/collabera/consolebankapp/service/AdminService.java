package com.collabera.consolebankapp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@Service
public class AdminService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserCredentialRepository userCredentialRepository,
            PasswordEncoder passwordEncoder) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserCredential createAdmin(String username, String password) {
        String cleanUsername = requireText(username, "Username");
        String cleanPassword = requirePassword(password);

        if (userCredentialRepository.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new DuplicateResourceException("Username is already in use");
        }

        UserCredential admin = new UserCredential(
                cleanUsername,
                passwordEncoder.encode(cleanPassword),
                Role.ADMIN,
                null);
        return userCredentialRepository.save(admin);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        return password;
    }
}
