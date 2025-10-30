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
public class QuickViewIncomeAndExpenseDetailDto {
  private BigDecimal occured;
  private BigDecimal waiting;
  private BigDecimal planning;
  private Double lastMonthChangeRate;
}
