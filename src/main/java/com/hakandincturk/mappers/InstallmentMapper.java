package com.hakandincturk.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hakandincturk.dtos.installment.response.ListMySpecificDateInstallmentsResponseDto;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.models.Installment;

@Mapper(componentModel = "spring", uses = {TransactionMapper.class})
public interface InstallmentMapper {

  @Mapping(target = "transaction", source = "transaction")
  ListMySpecificDateInstallmentsResponseDto toListMySpecificDateInstallmentsResponseDto(Installment installment);

  ListInstallments toListTransactionIntallments(Installment installment);

}