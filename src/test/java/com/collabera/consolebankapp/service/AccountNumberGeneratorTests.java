package com.collabera.consolebankapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountNumberGeneratorTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SecureRandom secureRandom;

    private AccountNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AccountNumberGenerator(accountRepository, secureRandom);
    }

    @Test
    void generatesSevenDigitCheckingNumber() {
        when(secureRandom.nextInt(10_000_000)).thenReturn(123);
        when(accountRepository.existsByAccountNumberIgnoreCase("CHK-0000123"))
                .thenReturn(false);

        assertEquals("CHK-0000123", generator.generate(AccountType.CHECKING));
    }

    @Test
    void generatesSavingsNumberAndRetriesCollision() {
        when(secureRandom.nextInt(10_000_000)).thenReturn(1234567, 7654321);
        when(accountRepository.existsByAccountNumberIgnoreCase("SAV-1234567"))
                .thenReturn(true);
        when(accountRepository.existsByAccountNumberIgnoreCase("SAV-7654321"))
                .thenReturn(false);

        assertEquals("SAV-7654321", generator.generate(AccountType.SAVINGS));
    }
}
