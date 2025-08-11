package com.hakandincturk.webapi.controllers.concretes;

import java.util.List;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;

public interface AccountController {

  public ApiResponse<?> createAccount(CreateAccountRequestDto body);
  public ApiResponse<List<ListMyAccountsResponseDto>> listMyActiveAccounts();
  public ApiResponse<?> updateMyAccount(Long accountId, UpdateAccountRequestDto body);
  
}
