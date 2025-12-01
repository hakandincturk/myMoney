package com.hakandincturk.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>{
  Page<Transaction> findByUserIdAndIsRemovedFalse(Long userId, Pageable pageData);
  Optional<Transaction> findByIdAndUserIdAndIsRemovedFalse(Long id, Long userId);

  @Query("""
    SELECT count(t) as count
    FROM Transaction t
    WHERE 
        t.user.id = :userId
        AND t.status IN (:statuses)
        AND t.type IN (:types)
        AND t.totalInstallment > 3
        AND COALESCE(t.totalAmount, 0) > 0
        AND ((t.totalAmount - t.paidAmount) / t.totalAmount > 0.15)
      """)
  Long findWaitingTransactionCount(
    @Param("userId") Long userId,
    @Param("statuses") List<TransactionStatuses> statuses,
    @Param("types") List<TransactionTypes> types
  );
}
