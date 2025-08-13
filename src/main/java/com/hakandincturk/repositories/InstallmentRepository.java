package com.hakandincturk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hakandincturk.models.Installment;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
  
}
