package com.collabera.consolebankapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collabera.consolebankapp.dto.AccountResponse;
import com.collabera.consolebankapp.dto.AmountRequest;
import com.collabera.consolebankapp.dto.TransferRequest;
import com.collabera.consolebankapp.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(@PathVariable String accountNumber) {
        return AccountResponse.from(accountService.getAccount(accountNumber));
    }

    @PostMapping("/{accountNumber}/deposits")
    public AccountResponse deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(accountService.deposit(accountNumber, request.amount()));
    }

    @PostMapping("/{accountNumber}/withdrawals")
    public AccountResponse withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(accountService.withdraw(accountNumber, request.amount()));
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        accountService.transfer(
                request.sourceAccountNumber(),
                request.destinationAccountNumber(),
                request.amount());
        return ResponseEntity.noContent().build();
    }
}
