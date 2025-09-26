package com.example.chequeprocessing.repository;

import com.example.chequeprocessing.domain.Cheque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChequeRepository extends JpaRepository<Cheque, Long> {
    Optional<Cheque> findByNumber(String number);
}


