package com.hakandincturk.webapi.controllers.concretes;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;

public interface TransactionController {
  public ApiResponse<?> createTransaction(CreateTransactionRequestDto body);
  public ApiResponse<?> deleteMyTransaction(Long transactionId);
  public ApiResponse<PagedResponse<ListMyTransactionsResponseDto>> listMyTransactions(SortablePageRequest pageData);
}