package com.hakandincturk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Installment;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {
  
}
