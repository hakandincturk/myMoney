package com.hakandincturk.services.abstracts;

import java.util.List;

import org.springframework.data.domain.Page;

import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.TagTransactionsFilterRequestDto;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.dtos.transaction.request.UpdateTransactionTagsRequestDto;
import com.hakandincturk.dtos.transaction.response.ListTagTransactionsResponseDto;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.dtos.transaction.response.UpdateTransactionTagsResponseDto;

public interface TransactionService {
  void createTransaction(Long userId, CreateTransactionRequestDto body);
  void deleteMyTransaction(Long userId, Long transactionId);
  Page<ListMyTransactionsResponseDto> listMyTransactions(Long userId, TransactionFilterRequestDto pageData);
  List<ListInstallments> listTransactionInstallments(Long userId, Long transactionId);
  public Page<ListTagTransactionsResponseDto> listTagTransactions(Long userId, Long tagId, TagTransactionsFilterRequestDto pageData);
  public UpdateTransactionTagsResponseDto updateTransactionTags(Long userId, Long transactionId, UpdateTransactionTagsRequestDto body);
}
