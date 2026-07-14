package com.collabera.consolebankapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collabera.consolebankapp.dto.AccountResponse;
import com.collabera.consolebankapp.dto.CreateAccountRequest;
import com.collabera.consolebankapp.dto.CreateCustomerRequest;
import com.collabera.consolebankapp.dto.CustomerResponse;
import com.collabera.consolebankapp.service.AccountService;
import com.collabera.consolebankapp.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerController(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = CustomerResponse.from(
                customerService.createCustomer(request.username(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomer(@PathVariable String customerId) {
        return CustomerResponse.from(customerService.getCustomer(customerId));
    }

    @PostMapping("/{customerId}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable String customerId,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = AccountResponse.from(accountService.createAccount(
                customerId,
                request.accountNumber(),
                request.type(),
                request.startingBalance()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}/accounts")
    public List<AccountResponse> getCustomerAccounts(@PathVariable String customerId) {
        return accountService.getAccountsForCustomer(customerId).stream()
                .map(AccountResponse::from)
                .toList();
    }
}
