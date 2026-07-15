package com.collabera.consolebankapp.service;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.repository.AccountRepository;

@Component
public class AccountNumberGenerator {

    private static final int ACCOUNT_NUMBER_RANGE = 10_000_000;
    private static final int MAX_GENERATION_ATTEMPTS = 20;

    private final AccountRepository accountRepository;
    private final SecureRandom secureRandom;

    @Autowired
    public AccountNumberGenerator(AccountRepository accountRepository) {
        this(accountRepository, new SecureRandom());
    }

    AccountNumberGenerator(AccountRepository accountRepository, SecureRandom secureRandom) {
        this.accountRepository = accountRepository;
        this.secureRandom = secureRandom;
    }

    public String generate(AccountType type) {
        if (type == null) {
            throw new IllegalArgumentException("Account type is required");
        }

        String prefix = switch (type) {
            case CHECKING -> "CHK-";
            case SAVINGS -> "SAV-";
        };

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = prefix + String.format(
                    Locale.ROOT, "%07d", secureRandom.nextInt(ACCOUNT_NUMBER_RANGE));
            if (!accountRepository.existsByAccountNumberIgnoreCase(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("A unique account number could not be generated");
    }
}
