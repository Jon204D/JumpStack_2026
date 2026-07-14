package com.collabera.consolebankapp.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.collabera.consolebankapp.exception.ForbiddenOperationException;
import com.collabera.consolebankapp.exception.ResourceNotFoundException;
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.model.Role;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.AccountRepository;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@ExtendWith(MockitoExtension.class)
class BankAuthorizationServiceTests {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private AccountRepository accountRepository;

    private BankAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new BankAuthorizationService(
                userCredentialRepository, accountRepository);
    }

    @Test
    void allowsAdminToAccessAnyCustomerOrAccount() {
        Authentication admin = authentication("admin", "ROLE_ADMIN");

        assertDoesNotThrow(() -> authorizationService.requireCustomerAccess(
                admin, "customer-2"));
        assertDoesNotThrow(() -> authorizationService.requireAccountAccess(
                admin, "CHK002"));
        verifyNoInteractions(userCredentialRepository, accountRepository);
    }

    @Test
    void allowsCustomerToAccessOwnCustomerRecord() {
        Authentication customer = authentication("customer1", "ROLE_CUSTOMER");
        when(userCredentialRepository.findByUsernameIgnoreCase("customer1"))
                .thenReturn(Optional.of(customerCredential("customer-1")));

        assertDoesNotThrow(() -> authorizationService.requireCustomerAccess(
                customer, "customer-1"));
    }

    @Test
    void forbidsCustomerFromAccessingAnotherCustomerRecord() {
        Authentication customer = authentication("customer1", "ROLE_CUSTOMER");
        when(userCredentialRepository.findByUsernameIgnoreCase("customer1"))
                .thenReturn(Optional.of(customerCredential("customer-1")));

        assertThrows(ForbiddenOperationException.class,
                () -> authorizationService.requireCustomerAccess(
                        customer, "customer-2"));
    }

    @Test
    void allowsCustomerToAccessOwnedAccount() {
        Authentication customer = authentication("customer1", "ROLE_CUSTOMER");
        when(userCredentialRepository.findByUsernameIgnoreCase("customer1"))
                .thenReturn(Optional.of(customerCredential("customer-1")));
        when(accountRepository.findByAccountNumberIgnoreCase("CHK001"))
                .thenReturn(Optional.of(account("CHK001", "customer-1")));

        assertDoesNotThrow(() -> authorizationService.requireAccountAccess(
                customer, "CHK001"));
    }

    @Test
    void forbidsCustomerFromAccessingAnotherCustomersAccount() {
        Authentication customer = authentication("customer1", "ROLE_CUSTOMER");
        when(userCredentialRepository.findByUsernameIgnoreCase("customer1"))
                .thenReturn(Optional.of(customerCredential("customer-1")));
        when(accountRepository.findByAccountNumberIgnoreCase("CHK002"))
                .thenReturn(Optional.of(account("CHK002", "customer-2")));

        assertThrows(ForbiddenOperationException.class,
                () -> authorizationService.requireAccountAccess(customer, "CHK002"));
    }

    @Test
    void reportsMissingAccountBeforeCheckingOwnership() {
        Authentication customer = authentication("customer1", "ROLE_CUSTOMER");
        when(userCredentialRepository.findByUsernameIgnoreCase("customer1"))
                .thenReturn(Optional.of(customerCredential("customer-1")));
        when(accountRepository.findByAccountNumberIgnoreCase("MISSING"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authorizationService.requireAccountAccess(customer, "MISSING"));
    }

    private Authentication authentication(String username, String authority) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "unused",
                List.of(new SimpleGrantedAuthority(authority)));
    }

    private UserCredential customerCredential(String customerId) {
        return new UserCredential(
                "customer1", "encoded", Role.CUSTOMER, customerId);
    }

    private Account account(String accountNumber, String customerId) {
        return new Account(
                accountNumber,
                customerId,
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                new BigDecimal("0.0100"));
    }
}
