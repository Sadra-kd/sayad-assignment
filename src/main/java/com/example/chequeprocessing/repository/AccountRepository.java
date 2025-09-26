package com.example.chequeprocessing.repository;

import com.example.chequeprocessing.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}


