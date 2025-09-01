package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>{
  Page<Transaction> findByUserIdAndIsRemovedFalse(Long userId, Pageable pageData);
  Optional<Transaction> findByIdAndUserIdAndIsRemovedFalse(Long id, Long userId);
}
