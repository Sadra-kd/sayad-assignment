package com.example.chequeprocessing.config;

import com.example.chequeprocessing.domain.Account;
import com.example.chequeprocessing.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedAccounts(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.count() == 0) {
                Account a1 = new Account();
                a1.setBalance(new BigDecimal("500000.00"));
                accountRepository.save(a1);

                Account a2 = new Account();
                a2.setBalance(new BigDecimal("1000.00"));
                accountRepository.save(a2);
            }
        };
    }
}


