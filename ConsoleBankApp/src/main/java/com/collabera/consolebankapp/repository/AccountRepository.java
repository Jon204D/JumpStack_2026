package com.collabera.consolebankapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.collabera.consolebankapp.model.Account;

public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByAccountNumberIgnoreCase(String accountNumber);

    List<Account> findByCustomerId(String customerId);

    boolean existsByAccountNumberIgnoreCase(String accountNumber);
}
