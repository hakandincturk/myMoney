package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.hakandincturk.core.enums.RecurringKinds;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tekrar eden tek bir harcama grubu.
 * groupKey, taksitli işlemlerde "TX:{transactionId}", isim eşleşmesinde "NAME:{normalizedName}" formatındadır.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringItemDto {

  private String groupKey;
  private String name;
  private RecurringKinds kind;
  private Integer occurrenceCount;
  private Integer monthsSpan;
  private BigDecimal averageAmount;
  private BigDecimal totalAmount;
  private BigDecimal lastAmount;
  private LocalDate lastDate;
  private LocalDate nextExpectedDate;
  private List<RecurringMonthAmountDto> amountByMonth;
  private List<ReportTagRefDto> tags;
  private boolean active;

}
