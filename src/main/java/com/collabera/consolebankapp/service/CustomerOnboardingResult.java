package com.collabera.consolebankapp.service;

import java.util.List;

import com.collabera.consolebankapp.model.Account;
import com.collabera.consolebankapp.model.Customer;

public record CustomerOnboardingResult(Customer customer, List<Account> accounts) {
}
