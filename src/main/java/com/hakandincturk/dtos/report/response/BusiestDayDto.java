package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ay içinde toplam giderin en yüksek olduğu tek gün.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusiestDayDto {

  private LocalDate date;
  private BigDecimal amount;

}
