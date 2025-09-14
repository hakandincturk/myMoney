package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;

public interface AccountController {
  ApiResponse<?> createAccount(CreateAccountRequestDto body);
  ApiResponse<PagedResponse<ListMyAccountsResponseDto>> listMyActiveAccounts(SortablePageRequest pageData);
  ApiResponse<?> updateMyAccount(Long accountId, UpdateAccountRequestDto body);
}
