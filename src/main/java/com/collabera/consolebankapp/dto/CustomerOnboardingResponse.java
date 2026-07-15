package com.collabera.consolebankapp.dto;

import java.util.List;

import com.collabera.consolebankapp.service.CustomerOnboardingResult;

public record CustomerOnboardingResponse(
        CustomerResponse customer,
        List<AccountResponse> accounts) {

    public static CustomerOnboardingResponse from(CustomerOnboardingResult result) {
        return new CustomerOnboardingResponse(
                CustomerResponse.from(result.customer()),
                result.accounts().stream().map(AccountResponse::from).toList());
    }
}
