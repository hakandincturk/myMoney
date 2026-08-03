package com.hakandincturk.dtos.report.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Grafik serisindeki tek bir ay.
 * Ay toplamlarına ek olarak iki farklı kalan borç metriği taşır; ikisi bilerek farklıdır.
 */
@Getter
@Setter
@NoArgsConstructor
public class TimelinePointDto extends PeriodTotalsDto {

  /**
   * Geçmişe bakan metrik: ayın sonunda fiilen taşınan borç.
   * Halen ödenmemiş tüm taksitler + o aydan sonra ödenmiş taksitler.
   * Gelecek aylarda düz gider, çünkü gelecekte hiçbir taksit henüz ödenmemiştir.
   */
  private BigDecimal cumulativeRemainingDebt;

  /**
   * Geleceğe bakan metrik: ayın sonundan sonraki vadeye sahip ve halen ödenmemiş taksitlerin toplamı.
   * "Plana uyulursa borç nasıl erir" eğrisidir, monoton azalır.
   */
  private BigDecimal projectedRemainingDebt;

  public TimelinePointDto(PeriodTotalsDto totals, BigDecimal cumulativeRemainingDebt, BigDecimal projectedRemainingDebt) {
    super(
      totals.getYear(),
      totals.getMonth(),
      totals.getLabel(),
      totals.getKind(),
      totals.getIncome(),
      totals.getExpense(),
      totals.getNet(),
      totals.getRealizedIncome(),
      totals.getPendingIncome(),
      totals.getRealizedExpense(),
      totals.getPendingExpense(),
      totals.getTransactionCount(),
      totals.getInstallmentCount()
    );
    this.cumulativeRemainingDebt = cumulativeRemainingDebt;
    this.projectedRemainingDebt = projectedRemainingDebt;
  }

}
