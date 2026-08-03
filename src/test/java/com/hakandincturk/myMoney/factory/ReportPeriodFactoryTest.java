package com.hakandincturk.myMoney.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.PeriodKind;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.report.response.PeriodTotalsDto;
import com.hakandincturk.factories.ReportPeriodFactory;
import com.hakandincturk.repositories.projections.MonthlyEntityCountProjection;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;

class ReportPeriodFactoryTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

  private final ReportPeriodFactory factory = new ReportPeriodFactory();

  @Test
  @DisplayName("Veri olmayan aylar sıfırlı kayıtla doldurulmalı")
  void buildPeriodTotals_shouldFillEmptyMonthsWithZero() {
    Map<YearMonth, PeriodTotalsDto> result = factory.buildPeriodTotals(
        YearMonth.of(2026, 4), YearMonth.of(2026, 6), List.of(), List.of(), TODAY);

    assertEquals(3, result.size());
    PeriodTotalsDto april = result.get(YearMonth.of(2026, 4));
    assertEquals(0, BigDecimal.ZERO.compareTo(april.getIncome()));
    assertEquals(0, BigDecimal.ZERO.compareTo(april.getExpense()));
    assertEquals(0, BigDecimal.ZERO.compareTo(april.getNet()));
    assertEquals(0, april.getTransactionCount());
    assertEquals(0, april.getInstallmentCount());
    assertEquals("APRIL", april.getLabel());
  }

  @Test
  @DisplayName("Dönem kesinlik seviyeleri aya göre atanmalı")
  void buildPeriodTotals_shouldAssignPeriodKinds() {
    Map<YearMonth, PeriodTotalsDto> result = factory.buildPeriodTotals(
        YearMonth.of(2026, 5), YearMonth.of(2026, 7), List.of(), List.of(), TODAY);

    assertEquals(PeriodKind.ACTUAL, result.get(YearMonth.of(2026, 5)).getKind());
    assertEquals(PeriodKind.PARTIAL, result.get(YearMonth.of(2026, 6)).getKind());
    assertEquals(PeriodKind.PROJECTED, result.get(YearMonth.of(2026, 7)).getKind());
  }

  @Test
  @DisplayName("Gelir ve gider tipleri gerçekleşen/bekleyen olarak ayrılmalı")
  void buildPeriodTotals_shouldSplitRealizedAndPending() {
    List<MonthlyTypeAmountProjection> amounts = List.of(
        new MonthlyTypeAmountProjection(2026, 6, TransactionTypes.CREDIT, true, BigDecimal.valueOf(40000)),
        new MonthlyTypeAmountProjection(2026, 6, TransactionTypes.COLLECTION, false, BigDecimal.valueOf(8500)),
        new MonthlyTypeAmountProjection(2026, 6, TransactionTypes.DEBT, true, BigDecimal.valueOf(30000)),
        new MonthlyTypeAmountProjection(2026, 6, TransactionTypes.PAYMENT, false, BigDecimal.valueOf(9300))
    );
    List<MonthlyEntityCountProjection> counts = List.of(
        new MonthlyEntityCountProjection(2026, 6, 42L, 57L)
    );

    PeriodTotalsDto totals = factory
        .buildPeriodTotals(YearMonth.of(2026, 6), YearMonth.of(2026, 6), amounts, counts, TODAY)
        .get(YearMonth.of(2026, 6));

    assertEquals(0, BigDecimal.valueOf(40000).compareTo(totals.getRealizedIncome()));
    assertEquals(0, BigDecimal.valueOf(8500).compareTo(totals.getPendingIncome()));
    assertEquals(0, BigDecimal.valueOf(30000).compareTo(totals.getRealizedExpense()));
    assertEquals(0, BigDecimal.valueOf(9300).compareTo(totals.getPendingExpense()));
    assertEquals(0, BigDecimal.valueOf(48500).compareTo(totals.getIncome()));
    assertEquals(0, BigDecimal.valueOf(39300).compareTo(totals.getExpense()));
    assertEquals(0, BigDecimal.valueOf(9200).compareTo(totals.getNet()));
    assertEquals(42, totals.getTransactionCount());
    assertEquals(57, totals.getInstallmentCount());
  }

  @Test
  @DisplayName("Aralık dışındaki projeksiyon satırları yok sayılmalı")
  void buildPeriodTotals_shouldIgnoreRowsOutsideRange() {
    List<MonthlyTypeAmountProjection> amounts = List.of(
        new MonthlyTypeAmountProjection(2025, 1, TransactionTypes.DEBT, true, BigDecimal.valueOf(1000))
    );

    PeriodTotalsDto totals = factory
        .buildPeriodTotals(YearMonth.of(2026, 6), YearMonth.of(2026, 6), amounts, List.of(), TODAY)
        .get(YearMonth.of(2026, 6));

    assertEquals(0, BigDecimal.ZERO.compareTo(totals.getExpense()));
  }
}
