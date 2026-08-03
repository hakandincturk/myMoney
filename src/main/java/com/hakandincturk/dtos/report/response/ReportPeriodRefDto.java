package com.hakandincturk.dtos.report.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bir dönemin sade referansı; label frontend'in i18n ay anahtarıdır.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportPeriodRefDto {

  private Integer year;
  private Integer month;
  private String label;

}
