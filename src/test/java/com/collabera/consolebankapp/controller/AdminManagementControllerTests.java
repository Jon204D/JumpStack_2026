package com.collabera.consolebankapp.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.collabera.consolebankapp.exception.DuplicateResourceException;
import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.service.AdminService;

class AdminManagementControllerTests {

    private AdminService adminService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adminService = org.mockito.Mockito.mock(AdminService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminManagementController(adminService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createsAdminWithoutReturningPasswordMaterial() throws Exception {
        when(adminService.createAdmin("admin2", "securePassword123"))
                .thenReturn(new UserCredential(
                        "admin2",
                        "bcrypt-hash",
                        Role.ADMIN,
                        null));

        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"admin2",
                                  "password":"securePassword123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("admin2"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(adminService).createAdmin("admin2", "securePassword123");
    }

    @Test
    void rejectsShortAdminPassword() throws Exception {
        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"admin2",
                                  "password":"short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password must be between 8 and 72 characters"));
    }

    @Test
    void reportsDuplicateAdminUsernameAsConflict() throws Exception {
        when(adminService.createAdmin("admin", "securePassword123"))
                .thenThrow(new DuplicateResourceException("Username is already in use"));

        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"admin",
                                  "password":"securePassword123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already in use"));
    }
}
