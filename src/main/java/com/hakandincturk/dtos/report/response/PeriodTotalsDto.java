package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;

import com.hakandincturk.core.enums.PeriodKind;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bir ayın toplamları. income = realizedIncome + pendingIncome, net = income - expense.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodTotalsDto {

  private Integer year;
  private Integer month;
  private String label;
  private PeriodKind kind;
  private BigDecimal income;
  private BigDecimal expense;
  private BigDecimal net;
  private BigDecimal realizedIncome;
  private BigDecimal pendingIncome;
  private BigDecimal realizedExpense;
  private BigDecimal pendingExpense;
  private Integer transactionCount;
  private Integer installmentCount;

}
