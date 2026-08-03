package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sparkline serisindeki tek ay. Tutarı olmayan aylar 0 ile doldurulur, seri deliksizdir.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringMonthAmountDto {

  private Integer year;
  private Integer month;
  private BigDecimal amount;

}
