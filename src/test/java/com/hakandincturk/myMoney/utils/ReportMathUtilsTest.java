package com.hakandincturk.myMoney.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.utils.ReportMathUtils;

class ReportMathUtilsTest {

  @Test
  @DisplayName("changeRate - artış oranı 2 ondalıkla hesaplanmalı")
  void changeRate_shouldCalculateIncrease() {
    Double result = ReportMathUtils.changeRate(BigDecimal.valueOf(6270), BigDecimal.valueOf(4680));

    assertEquals(33.97, result);
  }

  @Test
  @DisplayName("changeRate - azalış oranı negatif dönmeli")
  void changeRate_shouldCalculateDecrease() {
    Double result = ReportMathUtils.changeRate(BigDecimal.valueOf(800), BigDecimal.valueOf(1000));

    assertEquals(-20.00, result);
  }

  @Test
  @DisplayName("changeRate - önceki değer 0 ise null dönmeli")
  void changeRate_shouldReturnNull_whenPreviousIsZero() {
    assertNull(ReportMathUtils.changeRate(BigDecimal.valueOf(1000), BigDecimal.ZERO));
  }

  @Test
  @DisplayName("changeRate - önceki değer null ise null dönmeli")
  void changeRate_shouldReturnNull_whenPreviousIsNull() {
    assertNull(ReportMathUtils.changeRate(BigDecimal.valueOf(1000), null));
  }

  @Test
  @DisplayName("savingRate - gelir 0 ise 0 dönmeli")
  void savingRate_shouldReturnZero_whenIncomeIsZero() {
    assertEquals(0.0, ReportMathUtils.savingRate(BigDecimal.ZERO, BigDecimal.valueOf(500)));
  }

  @Test
  @DisplayName("savingRate - gider gelirden büyükse negatif dönmeli, clamp edilmemeli")
  void savingRate_shouldReturnNegative_whenExpenseExceedsIncome() {
    Double result = ReportMathUtils.savingRate(BigDecimal.valueOf(1000), BigDecimal.valueOf(1500));

    assertEquals(-50.00, result);
  }

  @Test
  @DisplayName("share - toplam 0 ise 0 dönmeli")
  void share_shouldReturnZero_whenTotalIsZero() {
    assertEquals(0.0, ReportMathUtils.share(BigDecimal.valueOf(100), BigDecimal.ZERO));
  }

  @Test
  @DisplayName("share - pay oranı yüzde olarak hesaplanmalı")
  void share_shouldCalculatePercentage() {
    assertEquals(21.37, ReportMathUtils.share(BigDecimal.valueOf(8400), BigDecimal.valueOf(39300)));
  }

  @Test
  @DisplayName("money - null değer sıfır olarak 2 ondalığa sabitlenmeli")
  void money_shouldNormalizeNull() {
    assertEquals(0, new BigDecimal("0.00").compareTo(ReportMathUtils.money(null)));
    assertEquals(2, ReportMathUtils.money(BigDecimal.TEN).scale());
  }

  @Test
  @DisplayName("divide - bölen 0 ise sıfır dönmeli")
  void divide_shouldReturnZero_whenDivisorIsZero() {
    assertEquals(0, BigDecimal.ZERO.compareTo(ReportMathUtils.divide(BigDecimal.TEN, 0)));
  }
}
