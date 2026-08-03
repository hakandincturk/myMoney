package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tekrar eden harcamalar.
 * monthlyFixedCost limit uygulanmadan tüm grupların aylık ortalamalarının toplamıdır.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRecurringResponseDto {

  private List<RecurringItemDto> items;
  private BigDecimal monthlyFixedCost;
  private Double fixedCostShareOfIncome;

  /** Tespit penceresinin ilk ayı; items boş olsa bile her zaman dolu gelir. */
  private ReportPeriodRefDto windowStart;

  /** Tespit penceresinin son ayı, yani sunucunun içinde bulunduğu ay. */
  private ReportPeriodRefDto windowEnd;

}
