package com.hakandincturk.services.abstracts;

import java.util.List;

import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;

public interface AccountService {
  public void createAccount(CreateAccountRequestDto body, Long userId);
  public List<ListMyAccountsResponseDto> listMyActiveAccounts(Long userId);
  public void updateMyAccount(Long userId, Long accountId, UpdateAccountRequestDto body);
}
