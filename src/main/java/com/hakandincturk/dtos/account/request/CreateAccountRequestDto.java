package com.hakandincturk.dtos.account.request;

import java.math.BigDecimal;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDto {
  
 @NotNull(message = "Ad boş olamaz")
  private String name;

  @NotNull(message = "Hesap tipi boş olamaz")
  private AccountTypes type;

  @NotNull(message = "Para birimi boş olamaz")
  private CurrencyTypes currency;

  @NotNull(message = "Bakiye boş olamaz")
  private BigDecimal balance;


}
