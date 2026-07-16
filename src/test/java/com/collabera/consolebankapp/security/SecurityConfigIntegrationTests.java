package com.collabera.consolebankapp.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
        "spring.mongodb.uri=mongodb://localhost:27017/console_bank_test",
        "spring.mongodb.database=console_bank_test",
        "spring.data.mongodb.auto-index-creation=false",
        "app.admin.password=",
        "debug=false",
        "logging.level.org.mongodb.driver=OFF"
})
class SecurityConfigIntegrationTests {

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void requiresAuthenticationForApiRequests() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requiresAuthenticationForCurrentUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void forbidsCustomerFromAdminOnlyEndpoint() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void forbidsCustomerFromListingEveryAccount() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void forbidsCustomerFromBankWideTransactionAudit() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void forbidsCustomerFromDeletingResources() throws Exception {
        mockMvc.perform(delete("/api/accounts/CHK001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void forbidsCustomerFromUpdatingCustomerCredentials() throws Exception {
        mockMvc.perform(patch("/api/customers/customer-1")
                        .contentType("application/json")
                        .content("{\"username\":\"customer2\"}"))
                .andExpect(status().isForbidden());
    }
}
