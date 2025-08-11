package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hakandincturk.models.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

  List<Account> findByUserIdAndIsRemovedFalseOrderByName(Long userId);
  Optional<Account> findByIdAndUserIdAndIsRemovedFalse(Long accountId, Long userId);
  
}
