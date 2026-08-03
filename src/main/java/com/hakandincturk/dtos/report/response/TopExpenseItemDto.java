package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ayın en büyük kalemlerinden biri; tutar işlemin toplamı değil, taksitin o aya düşen tutarıdır.
 * Tek çekim işlemlerde de installmentNumber = 1 ve totalInstallment = 1 olarak döner.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopExpenseItemDto {

  private Long transactionId;
  private Long installmentId;
  private String name;
  private String description;
  private BigDecimal amount;
  private LocalDate date;
  private String accountName;
  private String contactName;
  private List<ReportTagRefDto> tags;
  private Integer installmentNumber;
  private Integer totalInstallment;
  private boolean paid;

}
