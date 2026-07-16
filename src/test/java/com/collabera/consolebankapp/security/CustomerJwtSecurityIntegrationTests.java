package com.collabera.consolebankapp.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.UserCredentialRepository;
import com.collabera.consolebankapp.service.AccountService;

@SpringBootTest(properties = {
        "spring.mongodb.uri=mongodb://localhost:27017/console_bank_test",
        "spring.mongodb.database=console_bank_test",
        "spring.data.mongodb.auto-index-creation=false",
        "app.admin.password=",
        "app.jwt.secret=test-jwt-secret-that-is-definitely-at-least-32-bytes-long",
        "debug=false",
        "logging.level.org.mongodb.driver=OFF"
})
class CustomerJwtSecurityIntegrationTests {
    private static final String USERNAME = "customer1";
    private static final String CUSTOMER_ID = "customer-1";

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UserCredentialRepository userCredentialRepository;

    @MockitoBean
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        UserCredential credential = new UserCredential(
                USERNAME, "password-hash", Role.CUSTOMER, CUSTOMER_ID);
        when(userCredentialRepository.findByUsernameIgnoreCase(USERNAME))
                .thenReturn(Optional.of(credential));
    }

    @Test
    void returnsAuthenticatedCustomerIdentity() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + customerToken(Instant.now().plusSeconds(300))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.customerId").value(CUSTOMER_ID));
    }

    @Test
    void allowsCustomerToViewOwnedAccounts() throws Exception {
        when(accountService.getAccountsForCustomer(CUSTOMER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/customers/{customerId}/accounts", CUSTOMER_ID)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + customerToken(Instant.now().plusSeconds(300))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void forbidsCustomerFromViewingAnotherCustomersAccounts() throws Exception {
        mockMvc.perform(get("/api/customers/{customerId}/accounts", "customer-2")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + customerToken(Instant.now().plusSeconds(300))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You cannot access another customer's information"));
    }

    @Test
    void forbidsCustomerFromListingAllCustomers() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + customerToken(Instant.now().plusSeconds(300))))
                .andExpect(status().isForbidden());
    }

    @Test
    void forbidsCustomerFromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + customerToken(Instant.now().plusSeconds(300))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Forbidden"));
    }

    @Test
    void forbidsCustomerFromCreatingAdmin() throws Exception {
        mockMvc.perform(post("/api/admins")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + customerToken(Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"admin2",
                                  "password":"securePassword123"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsExpiredCustomerToken() throws Exception {
        mockMvc.perform(get("/api/customers/{customerId}/accounts", CUSTOMER_ID)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + customerToken(Instant.now().minusSeconds(60))))
                .andExpect(status().isUnauthorized());
    }

    private String customerToken(Instant expiresAt) {
        Instant issuedAt = expiresAt.isAfter(Instant.now())
                ? Instant.now()
                : expiresAt.minusSeconds(60);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("console-bank-app")
                .subject(USERNAME)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("roles", List.of("CUSTOMER"))
                .claim("customerId", CUSTOMER_ID)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
