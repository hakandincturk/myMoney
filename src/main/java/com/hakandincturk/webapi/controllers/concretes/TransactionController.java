package com.hakandincturk.webapi.controllers.concretes;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;

public interface TransactionController {
  public ApiResponse<?> createTransaction(CreateTransactionRequestDto body);
}