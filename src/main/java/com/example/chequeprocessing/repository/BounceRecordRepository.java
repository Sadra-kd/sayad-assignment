package com.example.chequeprocessing.repository;

import com.example.chequeprocessing.domain.BounceRecord;
import com.example.chequeprocessing.domain.Cheque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BounceRecordRepository extends JpaRepository<BounceRecord, Long> {
    List<BounceRecord> findByCheque(Cheque cheque);

    @Query("select br from BounceRecord br where br.cheque.drawer.id = :drawerId and br.date >= :since")
    List<BounceRecord> findBouncesForDrawerSince(@Param("drawerId") Long drawerId, @Param("since") LocalDate since);

    @Query("select count(br) from BounceRecord br where br.cheque.drawer.id = :drawerId and br.date >= :since")
    long countBouncesForDrawerSince(@Param("drawerId") Long drawerId, @Param("since") LocalDate since);
}


