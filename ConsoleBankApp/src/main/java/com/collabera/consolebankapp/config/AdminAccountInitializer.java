package com.collabera.consolebankapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminAccountInitializer(
            UserCredentialRepository userCredentialRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:admin}") String adminUsername,
            @Value("${app.admin.password:}") String adminPassword) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminPassword.isBlank()) {
            LOGGER.warn("APP_ADMIN_PASSWORD is not set; no initial admin account was created");
            return;
        }

        if (userCredentialRepository.existsByUsernameIgnoreCase(adminUsername)) {
            return;
        }

        UserCredential admin = new UserCredential(
                adminUsername.trim(),
                passwordEncoder.encode(adminPassword),
                Role.ADMIN,
                null);
        userCredentialRepository.save(admin);
        LOGGER.info("Initial admin account '{}' was created", adminUsername);
    }
}
