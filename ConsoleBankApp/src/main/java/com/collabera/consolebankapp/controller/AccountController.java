package com.collabera.consolebankapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collabera.consolebankapp.dto.AccountResponse;
import com.collabera.consolebankapp.dto.AmountRequest;
import com.collabera.consolebankapp.dto.TransferRequest;
import com.collabera.consolebankapp.dto.TransactionResponse;
import com.collabera.consolebankapp.service.AccountService;
import com.collabera.consolebankapp.security.BankAuthorizationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final BankAuthorizationService authorizationService;

    public AccountController(
            AccountService accountService,
            BankAuthorizationService authorizationService) {
        this.accountService = accountService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(
            @PathVariable String accountNumber,
            Authentication authentication) {
        authorizationService.requireAccountAccess(authentication, accountNumber);
        return AccountResponse.from(accountService.getAccount(accountNumber));
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{accountNumber}/transactions")
    public List<TransactionResponse> getTransactionHistory(
            @PathVariable String accountNumber,
            Authentication authentication) {
        authorizationService.requireAccountAccess(authentication, accountNumber);
        return accountService.getTransactionHistory(accountNumber).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @PostMapping("/{accountNumber}/deposits")
    public AccountResponse deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request,
            Authentication authentication) {
        authorizationService.requireAccountAccess(authentication, accountNumber);
        return AccountResponse.from(accountService.deposit(accountNumber, request.amount()));
    }

    @PostMapping("/{accountNumber}/withdrawals")
    public AccountResponse withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request,
            Authentication authentication) {
        authorizationService.requireAccountAccess(authentication, accountNumber);
        return AccountResponse.from(accountService.withdraw(accountNumber, request.amount()));
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        authorizationService.requireAccountAccess(
                authentication, request.sourceAccountNumber());
        accountService.transfer(
                request.sourceAccountNumber(),
                request.destinationAccountNumber(),
                request.amount());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }
}
