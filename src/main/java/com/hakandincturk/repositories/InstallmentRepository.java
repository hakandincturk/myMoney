package com.hakandincturk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Installment;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {
  
  // start: 2025-08-01, end: 2025-08-31
  List<Installment> findByTransactionUserIdAndDebtDateBetween(Long userId, LocalDate start, LocalDate end);
}
