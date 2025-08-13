package com.hakandincturk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hakandincturk.models.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
  
}
