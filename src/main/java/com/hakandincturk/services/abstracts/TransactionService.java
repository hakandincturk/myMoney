package com.hakandincturk.services.abstracts;

import java.util.List;

import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;

public interface TransactionService {
  public void createTransaction(Long userId, CreateTransactionRequestDto body);
  public List<ListMyTransactionsResponseDto> listMyTransactions(Long userId);
  public void deleteMyTransaction(Long userId, Long transactionId);
}
