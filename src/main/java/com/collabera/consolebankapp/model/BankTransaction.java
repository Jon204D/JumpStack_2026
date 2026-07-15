package com.collabera.consolebankapp.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collection = "transactions")
public class BankTransaction {

    @Id
    private String id;

    private TransactionType type;

    @Indexed
    private String sourceAccountNumber;

    @Indexed
    private String destinationAccountNumber;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    private Instant createdAt;

    public BankTransaction() {
    }

    public BankTransaction(
            TransactionType type,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            Instant createdAt) {
        this.type = type;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
