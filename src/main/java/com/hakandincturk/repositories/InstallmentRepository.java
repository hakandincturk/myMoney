package com.hakandincturk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Installment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long>, JpaSpecificationExecutor<Installment> {
  Optional<Installment> findByIdAndTransactionUserIdAndIsRemovedFalse(Long id, Long userId);
  List<Installment> findByTransaction_UserIdOrderByDebtDate(Long userId);
  List<Installment> findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(Long userId, LocalDate starDate, LocalDate endDate);
  List<Installment> findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(Long userId, List<TransactionTypes> type, LocalDate starDate, LocalDate endDate);
  List<Installment> findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(Long userId, LocalDate starDate, LocalDate endDate);
  // start: 2025-08-01, end: 2025-08-31
  // Page<Installment> findByTransactionUserIdAndDebtDateBetweenAndIsRemovedFalse(Long userId, LocalDate start, LocalDate end, Pageable pageData);
}
