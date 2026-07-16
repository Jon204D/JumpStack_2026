package com.collabera.consolebankapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTests {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    private AdminService adminService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        adminService = new AdminService(userCredentialRepository, passwordEncoder);
    }

    @Test
    void createsAdminWithHashedPasswordAndNoCustomerLink() {
        when(userCredentialRepository.existsByUsernameIgnoreCase("admin2"))
                .thenReturn(false);
        when(userCredentialRepository.save(any(UserCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserCredential admin = adminService.createAdmin(
                "  admin2  ",
                "securePassword123");

        assertEquals("admin2", admin.getUsername());
        assertEquals(Role.ADMIN, admin.getRole());
        assertNull(admin.getCustomerId());
        assertTrue(passwordEncoder.matches(
                "securePassword123",
                admin.getPasswordHash()));
        verify(userCredentialRepository).save(admin);
    }

    @Test
    void rejectsAnExistingUsername() {
        when(userCredentialRepository.existsByUsernameIgnoreCase("admin"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> adminService.createAdmin("admin", "securePassword123"));

        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    void rejectsBlankUsernameBeforePersistence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminService.createAdmin("   ", "securePassword123"));

        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }
}
