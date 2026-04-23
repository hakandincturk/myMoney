package com.hakandincturk.dtos.transaction.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hakandincturk.core.enums.InstallmentStatuses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListInstallments {
  private Long id;
  private BigDecimal amount;
  private LocalDate debtDate;
  private int installmentNumber;
  private String description;
  private boolean isPaid;
  private InstallmentStatuses status;
}
