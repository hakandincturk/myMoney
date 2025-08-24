package com.hakandincturk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  Page<Account> findByUserIdAndIsRemovedFalse(Long userId, Pageable pageData);
  Optional<Account> findByIdAndUserIdAndIsRemovedFalse(Long accountId, Long userId);
  
}
