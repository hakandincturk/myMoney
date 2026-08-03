package com.hakandincturk.dtos.report.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRecurringRequestDto {

  public static final int DEFAULT_LOOKBACK_MONTHS = 6;
  public static final int MAX_LOOKBACK_MONTHS = 24;
  public static final int DEFAULT_MIN_OCCURRENCE = 3;
  public static final int MAX_MIN_OCCURRENCE = 24;
  public static final int DEFAULT_LIMIT = 10;
  public static final int MAX_LIMIT = 100;

  // Pencere geçmişe doğru bakar ve içinde bulunulan ayı da kapsar
  private Integer lookbackMonths = DEFAULT_LOOKBACK_MONTHS;

  private Integer minOccurrence = DEFAULT_MIN_OCCURRENCE;

  private Integer limit = DEFAULT_LIMIT;

}
