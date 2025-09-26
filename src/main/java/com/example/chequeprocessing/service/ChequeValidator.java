package com.example.chequeprocessing.service;

import com.example.chequeprocessing.domain.Account;
import com.example.chequeprocessing.domain.Cheque;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ChequeValidator {

    public void validateIssueRules(Account drawer, BigDecimal amount) {
        if (drawer == null) {
            throw new IllegalArgumentException("Drawer account not found");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (drawer.getBalance() == null || drawer.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds for issuance");
        }
    }

    public void validatePresentationWindow(Cheque cheque, LocalDate today) {
        LocalDate deadline = cheque.getIssueDate().plusMonths(6);
        if (today.isAfter(deadline)) {
            throw new IllegalStateException("Cheque is stale (older than 6 months)");
        }
    }

    public void validateNonBearerUnconditional(String chequeNumber) {
        if (chequeNumber == null || chequeNumber.isBlank()) {
            throw new IllegalArgumentException("Cheque number required");
        }
        String normalized = chequeNumber.toUpperCase();
        if (normalized.contains("BEARER")) {
            throw new IllegalArgumentException("Bearer cheques are not allowed");
        }
        if (normalized.contains(" IF ") || normalized.startsWith("IF ") || normalized.endsWith(" IF")) {
            throw new IllegalArgumentException("Conditional cheques are not allowed");
        }
    }
}


