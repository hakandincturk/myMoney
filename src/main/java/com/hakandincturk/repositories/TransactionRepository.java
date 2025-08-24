package com.hakandincturk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{
  public Page<Transaction> findByUserIdAndIsRemovedFalse(Long userId, Pageable pageData);
  public Optional<Transaction> findByIdAndUserIdAndIsRemovedFalse(Long id, Long userId);
}
