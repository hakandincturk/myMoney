package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Seçili ayın karşılaştırıldığı döneme göre farkı.
 * Oranlar karşılaştırılan dönem 0 ise null döner (frontend "—" gösterir).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeriodDeltaDto {

  private Double incomeChangeRate;
  private Double expenseChangeRate;
  private BigDecimal netChangeAmount;

}
