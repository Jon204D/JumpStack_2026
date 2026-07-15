package com.collabera.consolebankapp.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.collabera.consolebankapp.model.Customer;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    Optional<Customer> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
