package com.hakandincturk.dtos.account.response;

import java.math.BigDecimal;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListMyAccountsResponseDto {

  private Long id;
  
  private String name;

  private BigDecimal totalBalance;

  private BigDecimal balance;

  private CurrencyTypes currency;

  private AccountTypes type;
  
}
