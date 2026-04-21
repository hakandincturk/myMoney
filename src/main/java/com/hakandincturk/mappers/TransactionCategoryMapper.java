package com.hakandincturk.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hakandincturk.dtos.transaction.response.ListCategoryTransactionsResponseDto;
import com.hakandincturk.models.TransactionCategory;

@Mapper(componentModel = "spring")
public interface TransactionCategoryMapper {

  @Mapping(target = "name", source = "transaction.name")
  @Mapping(target = "accountName", source = "transaction.account.name")
  @Mapping(target = "type", source = "transaction.type")
  @Mapping(target = "status", source = "transaction.status")
  @Mapping(target = "totalAmount", source = "transaction.totalAmount")
  @Mapping(target = "paidAmount", source = "transaction.paidAmount")
  @Mapping(target = "totalInstallment", source = "transaction.totalInstallment")
  ListCategoryTransactionsResponseDto toListCategoryTransactionsResponseDto(TransactionCategory transactionCategory);

}
