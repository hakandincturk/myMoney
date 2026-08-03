package com.hakandincturk.myMoney.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.PeriodKind;
import com.hakandincturk.utils.ReportPeriodUtils;

class ReportPeriodUtilsTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

  @Test
  @DisplayName("PeriodKind - geçmiş ay ACTUAL olmalı")
  void kindOf_shouldReturnActual_forPastMonth() {
    assertEquals(PeriodKind.ACTUAL, ReportPeriodUtils.kindOf(YearMonth.of(2026, 5), TODAY));
  }

  @Test
  @DisplayName("PeriodKind - içinde bulunulan ay PARTIAL olmalı")
  void kindOf_shouldReturnPartial_forCurrentMonth() {
    assertEquals(PeriodKind.PARTIAL, ReportPeriodUtils.kindOf(YearMonth.of(2026, 6), TODAY));
  }

  @Test
  @DisplayName("PeriodKind - gelecek ay PROJECTED olmalı")
  void kindOf_shouldReturnProjected_forFutureMonth() {
    assertEquals(PeriodKind.PROJECTED, ReportPeriodUtils.kindOf(YearMonth.of(2026, 7), TODAY));
  }

  @Test
  @DisplayName("label - ay adı İngilizce ve büyük harf olmalı")
  void labelOf_shouldReturnUpperCaseEnglishMonthName() {
    assertEquals("JUNE", ReportPeriodUtils.labelOf(YearMonth.of(2026, 6)));
    assertEquals("JANUARY", ReportPeriodUtils.labelOf(YearMonth.of(2026, 1)));
  }

  @Test
  @DisplayName("monthsBetween - aralıktaki tüm aylar kronolojik dönmeli")
  void monthsBetween_shouldReturnAllMonthsInOrder() {
    List<YearMonth> months = ReportPeriodUtils.monthsBetween(YearMonth.of(2025, 12), YearMonth.of(2026, 2));

    assertEquals(3, months.size());
    assertEquals(YearMonth.of(2025, 12), months.get(0));
    assertEquals(YearMonth.of(2026, 2), months.get(2));
  }

  @Test
  @DisplayName("divisorDayCount - içinde bulunulan ayda bugüne kadarki gün sayısı kullanılmalı")
  void divisorDayCount_shouldUseElapsedDays_forCurrentMonth() {
    assertEquals(15, ReportPeriodUtils.divisorDayCount(YearMonth.of(2026, 6), TODAY));
  }

  @Test
  @DisplayName("divisorDayCount - diğer aylarda ayın tamamı kullanılmalı")
  void divisorDayCount_shouldUseFullMonth_forOtherMonths() {
    assertEquals(31, ReportPeriodUtils.divisorDayCount(YearMonth.of(2026, 5), TODAY));
  }

  @Test
  @DisplayName("normalize - negatif ve null değerler default'a düşmeli")
  void normalize_shouldFallBackToDefault() {
    assertEquals(6, ReportPeriodUtils.normalize(-3, 6, 0, 24));
    assertEquals(6, ReportPeriodUtils.normalize(null, 6, 0, 24));
  }

  @Test
  @DisplayName("normalize - üst sınırı aşan değer sınıra çekilmeli")
  void normalize_shouldClampToMax() {
    assertEquals(24, ReportPeriodUtils.normalize(100, 6, 0, 24));
  }

  @Test
  @DisplayName("normalize - geçerli değer korunmalı")
  void normalize_shouldKeepValidValue() {
    assertEquals(12, ReportPeriodUtils.normalize(12, 6, 0, 24));
  }
}
