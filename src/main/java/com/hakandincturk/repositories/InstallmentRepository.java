package com.hakandincturk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Installment;

import java.time.LocalDate;
import java.util.Optional;


@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {
  
  Optional<Installment> findByIdAndTransactionUserIdAndIsRemovedFalse(Long id, Long userId);
  // start: 2025-08-01, end: 2025-08-31
  Page<Installment> findByTransactionUserIdAndDebtDateBetweenAndIsRemovedFalse(Long userId, LocalDate start, LocalDate end, Pageable pageData);
}
