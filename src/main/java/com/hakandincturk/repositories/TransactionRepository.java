package com.hakandincturk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{
  
}
