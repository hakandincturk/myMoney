package com.hakandincturk.services.rules;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Account;
import com.hakandincturk.repositories.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountRules {

  private final AccountRepository accountRepository;

  public Account checkUserAccountExistAndGet(Long userId, Long accountId){
    Optional<Account> dbAccount = accountRepository.findByIdAndUserIdAndIsRemovedFalse(accountId, userId);
    if(dbAccount.isEmpty()){
      throw new NotFoundException("Hesap bulunamadı");
    }

    return dbAccount.get();
  }
  
}
