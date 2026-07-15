package com.collabera.consolebankapp.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.collabera.consolebankapp.exception.ForbiddenOperationException;
import com.collabera.consolebankapp.exception.ResourceNotFoundException;
import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.UserCredential;
import com.collabera.consolebankapp.repository.AccountRepository;
import com.collabera.consolebankapp.repository.UserCredentialRepository;

@Service
public class BankAuthorizationService {

    private final UserCredentialRepository userCredentialRepository;
    private final AccountRepository accountRepository;

    public BankAuthorizationService(
            UserCredentialRepository userCredentialRepository,
            AccountRepository accountRepository) {
        this.userCredentialRepository = userCredentialRepository;
        this.accountRepository = accountRepository;
    }

    public void requireCustomerAccess(Authentication authentication, String customerId) {
        if (isAdmin(authentication)) {
            return;
        }

        UserCredential credential = currentCredential(authentication);
        if (credential.getCustomerId() == null
                || !credential.getCustomerId().equals(customerId)) {
            throw new ForbiddenOperationException(
                    "You cannot access another customer's information");
        }
    }

    public void requireAccountAccess(Authentication authentication, String accountNumber) {
        if (isAdmin(authentication)) {
            return;
        }

        UserCredential credential = currentCredential(authentication);
        Account account = accountRepository.findByAccountNumberIgnoreCase(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account was not found"));

        if (credential.getCustomerId() == null
                || !credential.getCustomerId().equals(account.getCustomerId())) {
            throw new ForbiddenOperationException(
                    "You cannot access an account that you do not own");
        }
    }

    private UserCredential currentCredential(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenOperationException("Authentication is required");
        }

        return userCredentialRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ForbiddenOperationException(
                        "Authenticated user is not linked to a bank user"));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
