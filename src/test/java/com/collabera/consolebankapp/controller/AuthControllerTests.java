package com.collabera.consolebankapp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

class AuthControllerTests {

    private MockMvc mockMvc;
    private UserCredentialRepository userCredentialRepository;

    @BeforeEach
    void setUp() {
        userCredentialRepository = org.mockito.Mockito.mock(UserCredentialRepository.class);
        AuthController controller = new AuthController(userCredentialRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsAuthenticatedCustomerIdentity() throws Exception {
        UserCredential credential = new UserCredential(
                "customer1", "password-hash", Role.CUSTOMER, "customer-1");
        when(userCredentialRepository.findByUsernameIgnoreCase("customer1"))
                .thenReturn(Optional.of(credential));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "customer1",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(get("/api/auth/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("customer1"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.customerId").value("customer-1"));
    }

    @Test
    void returnsAuthenticatedAdminIdentity() throws Exception {
        UserCredential credential = new UserCredential(
                "admin", "password-hash", Role.ADMIN, null);
        when(userCredentialRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(credential));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(get("/api/auth/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.customerId").doesNotExist());
    }

    @Test
    void rejectsAuthenticatedUserWithoutBankCredential() throws Exception {
        when(userCredentialRepository.findByUsernameIgnoreCase("missing"))
                .thenReturn(Optional.empty());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "missing",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(get("/api/auth/me").principal(authentication))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Authenticated user is not linked to a bank user"));
    }
}
