package com.hakandincturk.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hakandincturk.dtos.installment.response.TransactionDetailDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.models.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

  @Mapping(target = "accountName", source = "account.name")
  @Mapping(target = "contactName", source = "contact.fullName")
  ListMyTransactionsResponseDto toListMyTransactionsResponseDto(Transaction transaction);

  TransactionDetailDto toTransactionDetailDto(Transaction transaction);

}
