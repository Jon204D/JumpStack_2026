package com.collabera.consolebankapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.collabera.consolebankapp.dto.InitialAccountRequest;
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.model.Customer;

@ExtendWith(MockitoExtension.class)
class CustomerOnboardingServiceTests {

    @Mock
    private CustomerService customerService;

    @Mock
    private AccountService accountService;

    private CustomerOnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        onboardingService = new CustomerOnboardingService(customerService, accountService);
    }

    @Test
    void onboardsCustomerWithCheckingAndSavingsAllocation() {
        Customer customer = org.mockito.Mockito.mock(Customer.class);
        when(customer.getId()).thenReturn("customer-1");
        when(customerService.createCustomer("customer1", "customer123"))
                .thenReturn(customer);
        when(accountService.createAccount(
                "customer-1", AccountType.CHECKING, new BigDecimal("25.00")))
                .thenReturn(account("CHK-1234567", AccountType.CHECKING, "25.00"));
        when(accountService.createAccount(
                "customer-1", AccountType.SAVINGS, new BigDecimal("75.00")))
                .thenReturn(account("SAV-7654321", AccountType.SAVINGS, "75.00"));

        CustomerOnboardingResult result = onboardingService.onboard(
                "customer1",
                "customer123",
                new BigDecimal("100.00"),
                List.of(
                        new InitialAccountRequest(
                                AccountType.CHECKING, new BigDecimal("25.00")),
                        new InitialAccountRequest(
                                AccountType.SAVINGS, new BigDecimal("75.00"))));

        assertEquals(2, result.accounts().size());
        assertEquals(new BigDecimal("25.00"), result.accounts().getFirst().getBalance());
        assertEquals(new BigDecimal("75.00"), result.accounts().getLast().getBalance());
    }

    @Test
    void rejectsOverAllocationBeforeCreatingCustomer() {
        List<InitialAccountRequest> accounts = List.of(
                new InitialAccountRequest(AccountType.CHECKING, new BigDecimal("25.00")),
                new InitialAccountRequest(AccountType.SAVINGS, new BigDecimal("76.00")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> onboardingService.onboard(
                        "customer1",
                        "customer123",
                        new BigDecimal("100.00"),
                        accounts));

        assertEquals(
                "Account allocations must equal the total starting balance",
                exception.getMessage());
        verify(customerService, never()).createCustomer(any(), any());
    }

    @Test
    void rejectsDuplicateInitialAccountTypes() {
        List<InitialAccountRequest> accounts = List.of(
                new InitialAccountRequest(AccountType.CHECKING, new BigDecimal("50.00")),
                new InitialAccountRequest(AccountType.CHECKING, new BigDecimal("50.00")));

        assertThrows(
                IllegalArgumentException.class,
                () -> onboardingService.onboard(
                        "customer1",
                        "customer123",
                        new BigDecimal("100.00"),
                        accounts));
        verify(customerService, never()).createCustomer(any(), any());
    }

    private Account account(String number, AccountType type, String balance) {
        return new Account(
                number,
                "customer-1",
                type,
                new BigDecimal(balance),
                type == AccountType.CHECKING
                        ? new BigDecimal("0.0100")
                        : new BigDecimal("0.0150"));
    }
}
