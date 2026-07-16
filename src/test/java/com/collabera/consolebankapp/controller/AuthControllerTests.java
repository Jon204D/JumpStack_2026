package com.collabera.consolebankapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.dto.JwtAuthenticationResponse;
import com.collabera.consolebankapp.repository.UserCredentialRepository;
import com.collabera.consolebankapp.security.JwtTokenService;

class AuthControllerTests {

    private MockMvc mockMvc;
    private UserCredentialRepository userCredentialRepository;
    private AuthenticationManager authenticationManager;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        userCredentialRepository = org.mockito.Mockito.mock(UserCredentialRepository.class);
        authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        jwtTokenService = org.mockito.Mockito.mock(JwtTokenService.class);
        AuthController controller = new AuthController(
                userCredentialRepository, authenticationManager, jwtTokenService);
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
    void returnsJwtWhenLoginCredentialsAreValid() throws Exception {
        UserCredential credential = new UserCredential(
                "admin", "password-hash", Role.ADMIN, null);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        JwtAuthenticationResponse token = new JwtAuthenticationResponse(
                "signed.jwt.value", "Bearer", 900, "admin", Role.ADMIN, null);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(userCredentialRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(credential));
        when(jwtTokenService.issueToken(authentication, credential))
                .thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed.jwt.value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void rejectsInvalidLoginCredentials() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
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
