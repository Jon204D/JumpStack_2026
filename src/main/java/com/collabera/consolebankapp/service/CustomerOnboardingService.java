package com.collabera.consolebankapp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collabera.consolebankapp.dto.InitialAccountRequest;
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.model.Customer;

@Service
public class CustomerOnboardingService {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerOnboardingService(
            CustomerService customerService,
            AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @Transactional
    public CustomerOnboardingResult onboard(
            String username,
            String password,
            BigDecimal totalStartingBalance,
            List<InitialAccountRequest> initialAccounts) {
        BigDecimal cleanTotal = normalizeMoney(
                totalStartingBalance, "Total starting balance");

        if (initialAccounts == null || initialAccounts.isEmpty()) {
            throw new IllegalArgumentException("At least one initial account is required");
        }
        if (initialAccounts.size() > 2) {
            throw new IllegalArgumentException("No more than two initial accounts are allowed");
        }

        Set<AccountType> accountTypes = new HashSet<>();
        BigDecimal allocated = BigDecimal.ZERO.setScale(2);

        for (InitialAccountRequest request : initialAccounts) {
            if (request == null || request.type() == null) {
                throw new IllegalArgumentException("Account type is required");
            }
            if (!accountTypes.add(request.type())) {
                throw new IllegalArgumentException(
                        "Each initial account type can be selected only once");
            }

            allocated = allocated.add(
                    normalizeMoney(request.startingBalance(), "Starting balance"));
        }

        if (allocated.compareTo(cleanTotal) != 0) {
            throw new IllegalArgumentException(
                    "Account allocations must equal the total starting balance");
        }

        Customer customer = customerService.createCustomer(username, password);
        List<Account> accounts = initialAccounts.stream()
                .map(request -> accountService.createAccount(
                        customer.getId(),
                        request.type(),
                        request.startingBalance()))
                .toList();

        return new CustomerOnboardingResult(customer, accounts);
    }

    private BigDecimal normalizeMoney(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }

        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    fieldName + " cannot have more than two decimal places", exception);
        }
    }
}
