package com.hakandincturk.dtos.installment.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ListMySpecisifDateInstallmentsResponseDto {

  private String transactionName;
  private BigDecimal amount;
  private LocalDate date;
  private int installmentNumber;
  private String descripton;
  private boolean isPaid;
  private LocalDate paidDate;
  
}
