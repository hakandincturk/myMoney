package com.hakandincturk.dtos.dashboard.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuickViewResponseDto {

  private BigDecimal totalBalance;
  private QuickViewIncomeAndExpenseDetailDto income;
  private QuickViewIncomeAndExpenseDetailDto expense;
  private Double savingRate;
  private Integer waitingInstallments;

}
