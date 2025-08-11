package com.hakandincturk.dtos.account.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequestDto {
  @NotNull(message = "Ad boş olamaz")
  private String name;

  @NotNull(message = "Bakiye boş olamaz")
  private BigDecimal totalBalance;
}
