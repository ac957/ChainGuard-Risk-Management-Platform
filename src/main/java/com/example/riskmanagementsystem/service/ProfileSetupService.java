package com.example.riskmanagementsystem.service;

import org.springframework.stereotype.Service;

@Service
public class ProfileSetupService {

    public void validateOrgSetup(String orgAction,
                                 String orgId,
                                 String orgName) {

        if (orgAction == null || orgAction.isBlank()) {
            throw new IllegalArgumentException(
                    "Please select whether to join or create "
                            + "an organisation.");
        }

        if ("join".equalsIgnoreCase(orgAction)) {
            if (orgId == null || orgId.isBlank()) {
                throw new IllegalArgumentException(
                        "Please select an organisation to join.");
            }
        } else if ("create".equalsIgnoreCase(orgAction)) {
            if (orgName == null
                    || orgName.trim().length() < 2) {
                throw new IllegalArgumentException(
                        "Organisation name must be at least "
                                + "2 characters.");
            }
            if (!orgName.trim().matches(".*[a-zA-Z].*")) {
                throw new IllegalArgumentException(
                        "Organisation name must contain "
                                + "meaningful text.");
            }
        } else {
            throw new IllegalArgumentException(
                    "Invalid organisation action.");
        }
    }
}