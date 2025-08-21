package com.hakandincturk.webapi.controllers.concretes;

import java.util.List;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;

public interface TransactionController {
  public ApiResponse<?> createTransaction(CreateTransactionRequestDto body);
  public ApiResponse<List<ListMyTransactionsResponseDto>> listMyTransactions();
}