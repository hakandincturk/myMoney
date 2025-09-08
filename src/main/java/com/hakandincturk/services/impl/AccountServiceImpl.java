package com.hakandincturk.services.impl;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.services.abstracts.AccountService;
import com.hakandincturk.services.rules.AccountRules;
import com.hakandincturk.services.rules.UserRules;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;
  private final AccountRules accountRules;
  private final UserRules userRules;

  @Override
  public void createAccount(CreateAccountRequestDto body, Long userId) {
    User user = userRules.checkUserExistAndGet(userId);
    
    Account createdAccount = new Account(
      body.getName(),
      body.getType(),
      body.getCurrency(),
      body.getBalance(),
      body.getBalance(),
      user
    );

    accountRepository.save(createdAccount);
  }

  @Override
  public Page<ListMyAccountsResponseDto> listMyActiveAccounts(Long userId, Pageable pageData) {
    Page<Account> dbAccounts = accountRepository.findByUserIdAndIsRemovedFalse(userId, pageData);

    Page<ListMyAccountsResponseDto> accounts = dbAccounts.map(account -> new ListMyAccountsResponseDto(
      account.getId(),
      account.getName(),
      account.getTotalBalance(),
      account.getBalance(),
      account.getCurrency(),
      account.getType()
    ));

    return accounts;
  }

  @Override
  public void updateMyAccount(Long userId, Long accountId, UpdateAccountRequestDto body) {
    Account dbAccount = accountRules.checkUserAccountExistAndGet(userId, accountId);
    
    // if(dbAccount.getTotalBalance().compareTo(body.getTotalBalance()) > 0) {
    //   BigDecimal difference = dbAccount.getTotalBalance().subtract(body.getTotalBalance());
    //   BigDecimal newBalance = dbAccount.getBalance().subtract(difference);
    //   dbAccount.setBalance(newBalance);
    // }
    // else {
    //   BigDecimal difference = body.getTotalBalance().subtract(dbAccount.getTotalBalance());
    //   BigDecimal newBalance = dbAccount.getBalance().add(difference);
    //   dbAccount.setBalance(newBalance);
    // }
    
    // recalculating balance
    BigDecimal difference = body.getTotalBalance().subtract(dbAccount.getTotalBalance());
    BigDecimal newBalance = dbAccount.getBalance().add(difference);
    dbAccount.setBalance(newBalance);

    dbAccount.setName(body.getName());
    dbAccount.setTotalBalance(body.getTotalBalance());

    accountRepository.save(dbAccount);
  }
}
