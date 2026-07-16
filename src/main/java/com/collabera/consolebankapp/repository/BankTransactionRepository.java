package com.collabera.consolebankapp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.collabera.consolebankapp.model.BankTransaction;

public interface BankTransactionRepository
        extends MongoRepository<BankTransaction, String> {

    List<BankTransaction> findAllByOrderByCreatedAtDesc();

    List<BankTransaction>
            findBySourceAccountNumberIgnoreCaseOrDestinationAccountNumberIgnoreCaseOrderByCreatedAtDesc(
                    String sourceAccountNumber,
                    String destinationAccountNumber);
}
