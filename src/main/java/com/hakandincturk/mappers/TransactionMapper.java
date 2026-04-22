package com.hakandincturk.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hakandincturk.dtos.dashboard.response.LastTransactionDataDto;
import com.hakandincturk.dtos.installment.response.TransactionDetailDto;
import com.hakandincturk.dtos.tag.response.TransactionTagInfoDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

  @Mapping(target = "accountName", source = "account.name")
  @Mapping(target = "contactName", source = "contact.fullName")
  @Mapping(target = "tags", source = "transactionTags")
  ListMyTransactionsResponseDto toListMyTransactionsResponseDto(Transaction transaction);

  TransactionDetailDto toTransactionDetailDto(Transaction transaction);

  LastTransactionDataDto toLastTransactionDataDto(Transaction transaction);

  @Mapping(target = "id", source = "tag.id")
  @Mapping(target = "name", source = "tag.name")
  TransactionTagInfoDto toTransactionTagInfoDto(TransactionTag transactionTag);

}
