package com.collabera.consolebankapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_credentials")
public class UserCredential {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;
    private Role role;
    private String customerId;

    public UserCredential() {
    }

    public UserCredential(String username, String passwordHash, Role role, String customerId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.customerId = customerId;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getCustomerId() {
        return customerId;
    }
}
