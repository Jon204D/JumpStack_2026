package com.collabera.consolebankapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.collabera.consolebankapp.exception.InsufficientFundsException;
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.AccountType;
import com.collabera.consolebankapp.model.BankTransaction;
import com.collabera.consolebankapp.model.TransactionType;
import com.collabera.consolebankapp.repository.AccountRepository;
import com.collabera.consolebankapp.repository.BankTransactionRepository;
import com.collabera.consolebankapp.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankTransactionRepository transactionRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(
                accountRepository,
                customerRepository,
                transactionRepository,
                accountNumberGenerator);
    }

    @Test
    void createsSavingsAccountForExistingCustomer() {
        when(customerRepository.existsById("customer-1")).thenReturn(true);
        when(accountNumberGenerator.generate(AccountType.SAVINGS))
                .thenReturn("SAV-1234567");
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.createAccount(
                "customer-1",
                AccountType.SAVINGS,
                new BigDecimal("500"));

        assertEquals("SAV-1234567", account.getAccountNumber());
        assertEquals(AccountType.SAVINGS, account.getType());
        assertEquals(new BigDecimal("500.00"), account.getBalance());
        assertEquals(new BigDecimal("0.0150"), account.getInterestRate());
    }

    @Test
    void depositsIntoAccount() {
        Account account = checkingAccount("CHK001", "100.00");
        when(accountRepository.findByAccountNumberIgnoreCase("CHK001"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account updated = accountService.deposit("chk001", new BigDecimal("25.50"));

        assertEquals(new BigDecimal("125.50"), updated.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(
                org.mockito.ArgumentMatchers.argThat(transaction ->
                        transaction.getType() == TransactionType.DEPOSIT
                                && transaction.getSourceAccountNumber() == null
                                && transaction.getDestinationAccountNumber().equals("CHK001")
                                && transaction.getAmount().equals(new BigDecimal("25.50"))));
    }

    @Test
    void rejectsWithdrawalWhenBalanceIsInsufficient() {
        Account account = checkingAccount("CHK001", "100.00");
        when(accountRepository.findByAccountNumberIgnoreCase("CHK001"))
                .thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.withdraw("CHK001", new BigDecimal("100.01")));
        assertEquals(new BigDecimal("100.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void withdrawsFromAccountAndRecordsHistory() {
        Account account = checkingAccount("CHK001", "100.00");
        when(accountRepository.findByAccountNumberIgnoreCase("CHK001"))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account updated = accountService.withdraw("CHK001", new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), updated.getBalance());
        verify(transactionRepository).save(
                org.mockito.ArgumentMatchers.argThat(transaction ->
                        transaction.getType() == TransactionType.WITHDRAWAL
                                && transaction.getSourceAccountNumber().equals("CHK001")
                                && transaction.getDestinationAccountNumber() == null
                                && transaction.getAmount().equals(new BigDecimal("40.00"))));
    }

    @Test
    void transfersBetweenDifferentAccounts() {
        Account source = checkingAccount("CHK001", "100.00");
        Account destination = checkingAccount("CHK002", "20.00");
        when(accountRepository.findByAccountNumberIgnoreCase("CHK001"))
                .thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumberIgnoreCase("CHK002"))
                .thenReturn(Optional.of(destination));

        accountService.transfer("CHK001", "CHK002", new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), source.getBalance());
        assertEquals(new BigDecimal("50.00"), destination.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(
                org.mockito.ArgumentMatchers.argThat(transaction ->
                        transaction.getType() == TransactionType.TRANSFER
                                && transaction.getSourceAccountNumber().equals("CHK001")
                                && transaction.getDestinationAccountNumber().equals("CHK002")
                                && transaction.getAmount().equals(new BigDecimal("30.00"))));
    }

    @Test
    void rejectsAmountsSmallerThanOneCent() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit("CHK001", new BigDecimal("0.001")));
        verify(accountRepository, never()).findByAccountNumberIgnoreCase(any());
    }

    @Test
    void listsAccountsForExistingCustomer() {
        when(customerRepository.existsById("customer-1")).thenReturn(true);
        when(accountRepository.findByCustomerId("customer-1"))
                .thenReturn(List.of(checkingAccount("CHK001", "100.00")));

        List<Account> accounts = accountService.getAccountsForCustomer("customer-1");

        assertEquals(1, accounts.size());
    }

    @Test
    void listsAllAccountsForAdmin() {
        when(accountRepository.findAll())
                .thenReturn(List.of(checkingAccount("CHK001", "100.00")));

        List<Account> accounts = accountService.getAllAccounts();

        assertEquals(1, accounts.size());
    }

    @Test
    void returnsTransactionHistoryNewestFirst() {
        Account account = checkingAccount("CHK001", "100.00");
        BankTransaction transaction = new BankTransaction(
                TransactionType.DEPOSIT,
                null,
                "CHK001",
                new BigDecimal("25.00"),
                java.time.Instant.parse("2026-07-14T12:00:00Z"));
        when(accountRepository.findByAccountNumberIgnoreCase("CHK001"))
                .thenReturn(Optional.of(account));
        when(transactionRepository
                .findBySourceAccountNumberIgnoreCaseOrDestinationAccountNumberIgnoreCaseOrderByCreatedAtDesc(
                        "CHK001", "CHK001"))
                .thenReturn(List.of(transaction));

        List<BankTransaction> history = accountService.getTransactionHistory("chk001");

        assertEquals(1, history.size());
        assertEquals(TransactionType.DEPOSIT, history.getFirst().getType());
    }

    @Test
    void listsAllTransactionHistoryForAdminNewestFirst() {
        BankTransaction transaction = new BankTransaction(
                TransactionType.TRANSFER,
                "CHK001",
                "SAV001",
                new BigDecimal("30.00"),
                java.time.Instant.parse("2026-07-14T12:00:00Z"));
        when(transactionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(transaction));

        List<BankTransaction> history = accountService.getAllTransactionHistory();

        assertEquals(1, history.size());
        assertEquals(TransactionType.TRANSFER, history.getFirst().getType());
    }

    @Test
    void deletesExistingAccount() {
        Account account = checkingAccount("CHK001", "100.00");
        when(accountRepository.findByAccountNumberIgnoreCase("CHK001"))
                .thenReturn(Optional.of(account));

        accountService.deleteAccount("chk001");

        verify(accountRepository).delete(account);
    }

    private Account checkingAccount(String accountNumber, String balance) {
        return new Account(
                accountNumber,
                "customer-1",
                AccountType.CHECKING,
                new BigDecimal(balance),
                new BigDecimal("0.0100"));
    }
}
