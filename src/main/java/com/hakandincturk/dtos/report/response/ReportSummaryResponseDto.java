package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Seçilen ayın önceki ve sonraki ayla karşılaştırmalı özeti.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponseDto {

  private PeriodTotalsDto previous;
  private PeriodTotalsDto current;
  private PeriodTotalsDto next;

  /** previous -> current geçişi; oranların paydası previous. */
  private PeriodDeltaDto deltaVsPrevious;

  /** current -> next geçişi; oranların paydası current. */
  private PeriodDeltaDto deltaVsNext;

  private Double savingRate;

  /** Ayın gideri / daysElapsed. */
  private BigDecimal averageDailyExpense;

  /** Seçilen ayın toplam gün sayısı. */
  private Integer daysInMonth;

  /** averageDailyExpense hesabında kullanılan bölen; içinde bulunulan ayda bugüne kadarki gün sayısı. */
  private Integer daysElapsed;

  /** İçinde bulunulan ayda naif doğrusal ay sonu tahmini, diğer aylarda ayın kendi gideri. */
  private BigDecimal projectedMonthEndExpense;

  private BusiestDayDto busiestDay;
  private TopTagDto topTag;

}
