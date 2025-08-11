package com.hakandincturk.services.rules;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Account;
import com.hakandincturk.repositories.AccountRepository;

@Service
public class AccountRules {

  @Autowired
  private AccountRepository accountRepository;

  public Account checkUserAccountExistAndGet(Long userId, Long accountId){
    Optional<Account> dbAccount = accountRepository.findByIdAndUserIdAndIsRemovedFalse(accountId, userId);
    if(dbAccount.isEmpty()){
      throw new NotFoundException("Hesap bulunamadı");
    }

    return dbAccount.get();
  }
  
}
