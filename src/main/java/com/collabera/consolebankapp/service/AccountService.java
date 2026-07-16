package com.collabera.consolebankapp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collabera.consolebankapp.exception.InsufficientFundsException;
import com.collabera.consolebankapp.exception.ResourceNotFoundException;
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.model.BankTransaction;
import com.collabera.consolebankapp.model.TransactionType;
import com.collabera.consolebankapp.repository.AccountRepository;
import com.collabera.consolebankapp.repository.BankTransactionRepository;
import com.collabera.consolebankapp.repository.CustomerRepository;

@Service
public class AccountService {

    private static final BigDecimal CHECKING_INTEREST_RATE = new BigDecimal("0.0100");
    private static final BigDecimal SAVINGS_INTEREST_RATE = new BigDecimal("0.0150");

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    public AccountService(AccountRepository accountRepository,
            CustomerRepository customerRepository,
            BankTransactionRepository transactionRepository,
            AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    public Account createAccount(String customerId, AccountType type,
            BigDecimal startingBalance) {
        String cleanCustomerId = requireText(customerId, "Customer id");
        BigDecimal cleanBalance = requireNonNegativeMoney(startingBalance, "Starting balance");

        if (type == null) {
            throw new IllegalArgumentException("Account type is required");
        }
        if (!customerRepository.existsById(cleanCustomerId)) {
            throw new ResourceNotFoundException("Customer was not found");
        }
        String accountNumber = accountNumberGenerator.generate(type);

        Account account = new Account(
                accountNumber,
                cleanCustomerId,
                type,
                cleanBalance,
                interestRateFor(type));
        return accountRepository.save(account);
    }

    public Account getAccount(String accountNumber) {
        return findAccount(normalizeAccountNumber(accountNumber));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> getAccountsForCustomer(String customerId) {
        String cleanCustomerId = requireText(customerId, "Customer id");
        if (!customerRepository.existsById(cleanCustomerId)) {
            throw new ResourceNotFoundException("Customer was not found");
        }
        return accountRepository.findByCustomerId(cleanCustomerId);
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        BigDecimal cleanAmount = requirePositiveMoney(amount, "Deposit amount");
        Account account = findAccount(normalizeAccountNumber(accountNumber));
        account.setBalance(account.getBalance().add(cleanAmount));
        Account savedAccount = accountRepository.save(account);
        transactionRepository.save(new BankTransaction(
                TransactionType.DEPOSIT,
                null,
                account.getAccountNumber(),
                cleanAmount,
                Instant.now()));
        return savedAccount;
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        BigDecimal cleanAmount = requirePositiveMoney(amount, "Withdrawal amount");
        Account account = findAccount(normalizeAccountNumber(accountNumber));

        if (account.getBalance().compareTo(cleanAmount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(cleanAmount));
        Account savedAccount = accountRepository.save(account);
        transactionRepository.save(new BankTransaction(
                TransactionType.WITHDRAWAL,
                account.getAccountNumber(),
                null,
                cleanAmount,
                Instant.now()));
        return savedAccount;
    }

    @Transactional
    public void transfer(String sourceAccountNumber, String destinationAccountNumber,
            BigDecimal amount) {
        String cleanSourceNumber = normalizeAccountNumber(sourceAccountNumber);
        String cleanDestinationNumber = normalizeAccountNumber(destinationAccountNumber);
        BigDecimal cleanAmount = requirePositiveMoney(amount, "Transfer amount");

        if (cleanSourceNumber.equals(cleanDestinationNumber)) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        Account source = findAccount(cleanSourceNumber);
        Account destination = findAccount(cleanDestinationNumber);

        if (source.getBalance().compareTo(cleanAmount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        source.setBalance(source.getBalance().subtract(cleanAmount));
        destination.setBalance(destination.getBalance().add(cleanAmount));
        accountRepository.save(source);
        accountRepository.save(destination);
        transactionRepository.save(new BankTransaction(
                TransactionType.TRANSFER,
                source.getAccountNumber(),
                destination.getAccountNumber(),
                cleanAmount,
                Instant.now()));
    }

    public List<BankTransaction> getTransactionHistory(String accountNumber) {
        String cleanAccountNumber = normalizeAccountNumber(accountNumber);
        findAccount(cleanAccountNumber);
        return transactionRepository
                .findBySourceAccountNumberIgnoreCaseOrDestinationAccountNumberIgnoreCaseOrderByCreatedAtDesc(
                        cleanAccountNumber,
                        cleanAccountNumber);
    }

    public List<BankTransaction> getAllTransactionHistory() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = findAccount(normalizeAccountNumber(accountNumber));
        accountRepository.delete(account);
    }

    private Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumberIgnoreCase(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account was not found"));
    }

    private BigDecimal interestRateFor(AccountType type) {
        return switch (type) {
            case CHECKING -> CHECKING_INTEREST_RATE;
            case SAVINGS -> SAVINGS_INTEREST_RATE;
        };
    }

    private String normalizeAccountNumber(String accountNumber) {
        return requireText(accountNumber, "Account number").toUpperCase(Locale.ROOT);
    }

    private BigDecimal requirePositiveMoney(BigDecimal amount, String fieldName) {
        BigDecimal money = normalizeMoney(amount, fieldName);
        if (money.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return money;
    }

    private BigDecimal requireNonNegativeMoney(BigDecimal amount, String fieldName) {
        BigDecimal money = normalizeMoney(amount, fieldName);
        if (money.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return money;
    }

    private BigDecimal normalizeMoney(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    fieldName + " cannot have more than two decimal places", exception);
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim();
    }
}
