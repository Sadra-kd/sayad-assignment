package com.example.chequeprocessing.service;

import com.example.chequeprocessing.domain.Account;
import com.example.chequeprocessing.domain.Cheque;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ChequeValidatorTest {
    private ChequeValidator validator;

    @BeforeEach
    void setup() {
        validator = new ChequeValidator();
    }

    @Test
    void validateIssueRules_success() {
        Account a = new Account();
        a.setBalance(new BigDecimal("100"));
        assertDoesNotThrow(() -> validator.validateIssueRules(a, new BigDecimal("50")));
    }

    @Test
    void validateIssueRules_insufficient() {
        Account a = new Account();
        a.setBalance(new BigDecimal("10"));
        assertThrows(IllegalStateException.class, () -> validator.validateIssueRules(a, new BigDecimal("50")));
    }

    @Test
    void validatePresentationWindow_stale() {
        Cheque c = new Cheque();
        c.setIssueDate(LocalDate.now().minusMonths(7));
        assertThrows(IllegalStateException.class, () -> validator.validatePresentationWindow(c, LocalDate.now()));
    }

    @Test
    void nonBearerUnconditional_checks() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateNonBearerUnconditional("abc BEARER xyz"));
        assertThrows(IllegalArgumentException.class, () -> validator.validateNonBearerUnconditional("IF paid then valid"));
        assertDoesNotThrow(() -> validator.validateNonBearerUnconditional("YT-2025-0001"));
    }
}


