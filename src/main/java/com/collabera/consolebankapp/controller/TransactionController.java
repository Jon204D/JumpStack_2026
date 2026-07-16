package com.collabera.consolebankapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collabera.consolebankapp.dto.TransactionResponse;
import com.collabera.consolebankapp.service.AccountService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final AccountService accountService;

    public TransactionController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return accountService.getAllTransactionHistory().stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
