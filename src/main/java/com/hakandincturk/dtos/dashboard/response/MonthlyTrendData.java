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
public class MonthlyTrendData {
  private String title;
  private BigDecimal income;
  private BigDecimal expense;
}
