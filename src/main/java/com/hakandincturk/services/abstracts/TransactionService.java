package com.hakandincturk.services.abstracts;

import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;

public interface TransactionService {
  public void createTransaction(Long userId, CreateTransactionRequestDto body);
}
