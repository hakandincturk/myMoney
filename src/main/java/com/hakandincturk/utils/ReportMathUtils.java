package com.hakandincturk.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.experimental.UtilityClass;

/**
 * Rapor modülünün ortak matematik kuralları.
 * Tüm para alanları 2 ondalık, tüm oranlar 2 ondalık yuvarlanır.
 */
@UtilityClass
public class ReportMathUtils {

  public static final int MONEY_SCALE = 2;

  // Oran hesabında ara bölme hassasiyeti; 2 ondalığa yuvarlamadan önce bilgi kaybını engeller
  private static final int RATE_INTERNAL_SCALE = 8;

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  /**
   * Yüzde değişim: ((current - previous) / previous) * 100
   * Önceki değer 0 (veya null) ise sonsuz/NaN yerine null döner; frontend "—" gösterir.
   */
  public static Double changeRate(BigDecimal current, BigDecimal previous){
    if(previous == null || previous.signum() == 0){
      return null;
    }

    BigDecimal safeCurrent = zeroIfNull(current);
    return safeCurrent.subtract(previous)
      .divide(previous, RATE_INTERNAL_SCALE, RoundingMode.HALF_UP)
      .multiply(HUNDRED)
      .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
      .doubleValue();
  }

  /**
   * Payın toplama oranı (%). Toplam 0 ise 0 döner - bu oran contract gereği null olamaz.
   */
  public static Double share(BigDecimal part, BigDecimal total){
    if(total == null || total.signum() == 0){
      return Double.valueOf(0);
    }

    return zeroIfNull(part)
      .divide(total, RATE_INTERNAL_SCALE, RoundingMode.HALF_UP)
      .multiply(HUNDRED)
      .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
      .doubleValue();
  }

  /**
   * Tasarruf oranı: ((income - expense) / income) * 100
   * Negatif olabilir, clamp edilmez; gelir 0 ise 0 döner.
   */
  public static Double savingRate(BigDecimal income, BigDecimal expense){
    if(income == null || income.signum() == 0){
      return Double.valueOf(0);
    }

    return income.subtract(zeroIfNull(expense))
      .divide(income, RATE_INTERNAL_SCALE, RoundingMode.HALF_UP)
      .multiply(HUNDRED)
      .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
      .doubleValue();
  }

  public static BigDecimal divide(BigDecimal value, long divisor){
    if(divisor == 0){
      return money(BigDecimal.ZERO);
    }

    return zeroIfNull(value).divide(BigDecimal.valueOf(divisor), MONEY_SCALE, RoundingMode.HALF_UP);
  }

  public static BigDecimal money(BigDecimal value){
    return zeroIfNull(value).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }

  public static BigDecimal zeroIfNull(BigDecimal value){
    return value == null ? BigDecimal.ZERO : value;
  }
}
