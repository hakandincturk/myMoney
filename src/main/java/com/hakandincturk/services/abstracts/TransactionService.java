package com.hakandincturk.services.abstracts;

import java.util.List;

import org.springframework.data.domain.Page;

import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequest;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;

public interface TransactionService {
  public void createTransaction(Long userId, CreateTransactionRequestDto body);
  public void deleteMyTransaction(Long userId, Long transactionId);
  public Page<ListMyTransactionsResponseDto> listMyTransactions(Long userId, TransactionFilterRequest pageData);
  public List<ListInstallments> listTransactionInstallments(Long userId, Long transactionId);
}
