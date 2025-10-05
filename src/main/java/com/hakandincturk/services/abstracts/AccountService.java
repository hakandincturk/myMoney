package com.hakandincturk.services.abstracts;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;

public interface AccountService {
  public void createAccount(CreateAccountRequestDto body, Long userId);
  public Page<ListMyAccountsResponseDto> listMyActiveAccounts(Long userId, Pageable pageData);
  public void updateMyAccount(Long userId, Long accountId, UpdateAccountRequestDto body);
}
