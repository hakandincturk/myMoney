package com.hakandincturk.mappers;

import org.mapstruct.Mapper;

import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;
import com.hakandincturk.models.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {
  ListMyAccountsResponseDto toListMyAccountsResponseDto(Account account);
}
