package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  List<Account> findByUserIdAndIsRemovedFalseOrderByName(Long userId);
  Optional<Account> findByIdAndUserIdAndIsRemovedFalse(Long accountId, Long userId);
  
}
