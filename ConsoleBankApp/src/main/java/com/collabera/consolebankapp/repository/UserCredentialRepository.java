package com.collabera.consolebankapp.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.collabera.consolebankapp.model.UserCredential;

public interface UserCredentialRepository extends MongoRepository<UserCredential, String> {

    Optional<UserCredential> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
