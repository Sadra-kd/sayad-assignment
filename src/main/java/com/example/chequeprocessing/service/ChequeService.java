package com.example.chequeprocessing.service;

import com.example.chequeprocessing.domain.*;
import com.example.chequeprocessing.repository.AccountRepository;
import com.example.chequeprocessing.repository.BounceRecordRepository;
import com.example.chequeprocessing.repository.ChequeRepository;
import com.example.chequeprocessing.sayad.SayadClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class ChequeService {
    private final AccountRepository accountRepository;
    private final ChequeRepository chequeRepository;
    private final BounceRecordRepository bounceRecordRepository;
    private final SayadClient sayadClient;
    private final ChequeValidator chequeValidator;

    public ChequeService(AccountRepository accountRepository,
                         ChequeRepository chequeRepository,
                         BounceRecordRepository bounceRecordRepository,
                         SayadClient sayadClient,
                         ChequeValidator chequeValidator) {
        this.accountRepository = accountRepository;
        this.chequeRepository = chequeRepository;
        this.bounceRecordRepository = bounceRecordRepository;
        this.sayadClient = sayadClient;
        this.chequeValidator = chequeValidator;
    }

    @Transactional
    public Cheque issueCheque(Long drawerId, String number, BigDecimal amount, LocalDate today) {
        Account drawer = accountRepository.findById(drawerId)
                .orElseThrow(() -> new IllegalArgumentException("Drawer not found"));

        chequeValidator.validateNonBearerUnconditional(number);

        // SAYAD register
        if (!sayadClient.registerCheque(number)) {
            throw new IllegalStateException("SAYAD registration failed");
        }

        chequeValidator.validateIssueRules(drawer, amount);

        Cheque cheque = new Cheque();
        cheque.setNumber(number);
        cheque.setDrawer(drawer);
        cheque.setAmount(amount);
        cheque.setIssueDate(today);
        cheque.setStatus(ChequeStatus.ISSUED);
        chequeRepository.save(cheque);
        return cheque;
    }

    @Transactional(noRollbackFor = ChequeBounceException.class)
    public Cheque presentCheque(Long chequeId, LocalDate today) {
        Cheque cheque = chequeRepository.findById(chequeId)
                .orElseThrow(() -> new IllegalArgumentException("Cheque not found"));

        // SAYAD present
        if (!sayadClient.presentCheque(cheque.getNumber())) {
            throw new IllegalStateException("SAYAD present failed");
        }

        chequeValidator.validatePresentationWindow(cheque, today);

        Account drawer = cheque.getDrawer();
        if (drawer.getBalance().compareTo(cheque.getAmount()) < 0) {
            // Bounce
            BounceRecord br = new BounceRecord();
            br.setCheque(cheque);
            br.setDate(today);
            br.setReason("Insufficient funds");
            bounceRecordRepository.save(br);
            bounceRecordRepository.flush();

            // Count last 12 months bounces for this drawer
            LocalDate since = today.minus(Period.ofMonths(12));
            long recentBounces = bounceRecordRepository.countBouncesForDrawerSince(drawer.getId(), since);
            if (recentBounces >= 3) {
                drawer.setStatus(AccountStatus.BLOCKED);
                accountRepository.save(drawer);
            }

            cheque.setStatus(ChequeStatus.BOUNCED);
            chequeRepository.save(cheque);
            throw new ChequeBounceException("Cheque bounced");
        }

        // Pay
        drawer.setBalance(drawer.getBalance().subtract(cheque.getAmount()));
        accountRepository.save(drawer);
        cheque.setStatus(ChequeStatus.PAID);
        chequeRepository.save(cheque);
        return cheque;
    }

    public static class ChequeBounceException extends RuntimeException {
        public ChequeBounceException(String message) { super(message); }
    }
}


