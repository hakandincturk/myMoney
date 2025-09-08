package com.hakandincturk.dtos.installment.response;

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
public class ListMySpecificDateInstallmentsResponseDto {

  private Long id;
  private TransactionDetailDto transaction;
  private BigDecimal amount;
  private LocalDate debtDate;
  private int installmentNumber;
  private String descripton;
  private boolean isPaid;
  private LocalDate paidDate;
  
}
