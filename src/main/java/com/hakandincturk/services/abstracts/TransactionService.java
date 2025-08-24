package com.hakandincturk.services.abstracts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;

public interface TransactionService {
  public void createTransaction(Long userId, CreateTransactionRequestDto body);
  public Page<ListMyTransactionsResponseDto> listMyTransactions(Long userId, Pageable pageData);
  public void deleteMyTransaction(Long userId, Long transactionId);
}
