package com.hakandincturk.webapi.controllers.abstracts;

import java.util.List;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;

public interface TransactionController {
  ApiResponse<?> createTransaction(CreateTransactionRequestDto body);
  ApiResponse<?> deleteMyTransaction(Long transactionId);
  ApiResponse<PagedResponse<ListMyTransactionsResponseDto>> listMyTransactions(TransactionFilterRequestDto pageData);
  ApiResponse<List<ListInstallments>> listTransactionInstallments(Long transactionId);
}