package com.collabera.consolebankapp.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "spring.mongodb.uri=mongodb://localhost:27017/console_bank_test",
        "spring.mongodb.database=console_bank_test",
        "spring.data.mongodb.auto-index-creation=false",
        "app.admin.password=",
        "app.jwt.secret=test-jwt-secret-that-is-definitely-at-least-32-bytes-long",
        "debug=false",
        "logging.level.org.mongodb.driver=OFF"
})
class AdminJwtSecurityIntegrationTests {
    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private JwtEncoder jwtEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void allowsPublicEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Public endpoint is available"));
    }

    @Test
    void forbidsAdminEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Forbidden"));
    }

    @Test
    void allowsAdminToken() throws Exception {
        mockMvc.perform(get("/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("ADMIN", Instant.now().plusSeconds(300))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin access granted"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void forbidsCustomerToken() throws Exception {
        mockMvc.perform(get("/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("CUSTOMER", Instant.now().plusSeconds(300))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Forbidden"));
    }

    @Test
    void forbidsExpiredToken() throws Exception {
        mockMvc.perform(get("/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("ADMIN", Instant.now().minusSeconds(60))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Forbidden"));
    }

    private String token(String role, Instant expiresAt) {
        Instant issuedAt = expiresAt.isAfter(Instant.now())
                ? Instant.now()
                : expiresAt.minusSeconds(60);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("console-bank-app")
                .subject(role.equals("ADMIN") ? "admin" : "customer1")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("roles", List.of(role))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
