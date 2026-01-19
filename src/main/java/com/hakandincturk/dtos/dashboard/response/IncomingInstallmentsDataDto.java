package com.hakandincturk.dtos.dashboard.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomingInstallmentsDataDto {
  private IncomingInstallmentTransactionDetailDataDto transaction;
  private BigDecimal amount;
  private LocalDate debtDate; 
  private int installmentNumber;
  private int totalInstallment;
}
