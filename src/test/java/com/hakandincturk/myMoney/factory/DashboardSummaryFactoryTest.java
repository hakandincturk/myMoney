package com.hakandincturk.myMoney.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.dtos.dashboard.response.MonthlyTrendDataDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewIncomeAndExpenseDetailDto;
import com.hakandincturk.dtos.report.response.PeriodTotalsDto;
import com.hakandincturk.factories.DashboardSummaryFactory;
import com.hakandincturk.factories.ReportPeriodFactory;

class DashboardSummaryFactoryTest {

  private final DashboardSummaryFactory factory = new DashboardSummaryFactory();
  private final ReportPeriodFactory periodFactory = new ReportPeriodFactory();

  private static final YearMonth PERIOD = YearMonth.of(2026, 6);

  @Test
  @DisplayName("Gelir detayı - bekleyen vade bazlı, gerçekleşen ödeme bazlı; değişim vade toplamları arasında olmalı")
  void buildIncomeDetail_shouldMapWaitingOccurredAndDueBasedChange() {
    PeriodTotalsDto currentByDebtDate = totals("5000", "2000", "0", "0");
    PeriodTotalsDto previousByDebtDate = totals("5000", "1000", "0", "0");
    PeriodTotalsDto currentByPaidDate = totals("4000", "0", "0", "0");

    QuickViewIncomeAndExpenseDetailDto detail = factory.buildIncomeDetail(currentByDebtDate, previousByDebtDate, currentByPaidDate);

    assertEquals(0, new BigDecimal("2000").compareTo(detail.getWaiting()));
    assertEquals(0, new BigDecimal("4000").compareTo(detail.getOccured()));
    // Due this month 7000 vs. due last month 6000: (7000 - 6000) / 6000 * 100, same rounding as the reports
    assertEquals(16.67, detail.getLastMonthChangeRate());
  }

  @Test
  @DisplayName("Gider detayı - geçen ayın ödenmemiş taksitleri de tabana girmeli, azalışta negatif oran dönmeli")
  void buildExpenseDetail_shouldUseLastMonthsDueTotalAsBase() {
    PeriodTotalsDto currentByDebtDate = totals("0", "0", "300", "100");
    // Last month: 200 paid + 600 still unpaid; the old formula used only the 200 paid as the base
    PeriodTotalsDto previousByDebtDate = totals("0", "0", "200", "600");
    PeriodTotalsDto currentByPaidDate = totals("0", "0", "250", "0");

    QuickViewIncomeAndExpenseDetailDto detail = factory.buildExpenseDetail(currentByDebtDate, previousByDebtDate, currentByPaidDate);

    assertEquals(0, new BigDecimal("100").compareTo(detail.getWaiting()));
    assertEquals(0, new BigDecimal("250").compareTo(detail.getOccured()));
    // (400 - 800) / 800 = -50
    assertEquals(-50.0, detail.getLastMonthChangeRate());
  }

  @Test
  @DisplayName("Değişim oranı - geçen ay vadesi gelen bir şey yoksa null, aynıysa 0 dönmeli")
  void changeRate_shouldBeNullWithoutBase_andZeroWhenEqual() {
    PeriodTotalsDto current = totals("500", "0", "0", "0");
    PeriodTotalsDto empty = totals("0", "0", "0", "0");

    assertNull(factory.buildIncomeDetail(current, empty, empty).getLastMonthChangeRate());
    assertEquals(0.0, factory.buildIncomeDetail(current, totals("500", "0", "0", "0"), empty).getLastMonthChangeRate());
  }

  @Test
  @DisplayName("Trend - pencerenin her ayı sırayla gelmeli, veri olmayan ay 0 olmalı")
  void buildTrend_shouldFillEveryMonthInOrder() {
    Map<YearMonth, PeriodTotalsDto> totalsByMonth = Map.of(PERIOD.plusMonths(1), totals("100", "50", "30", "20"));

    List<MonthlyTrendDataDto> trend = factory.buildTrend(totalsByMonth, PERIOD, PERIOD.plusMonths(2));

    assertEquals(3, trend.size());
    assertEquals("JUNE", trend.get(0).getTitle());
    assertEquals(0, BigDecimal.ZERO.compareTo(trend.get(0).getIncome()));
    assertEquals("JULY", trend.get(1).getTitle());
    // Paid and waiting together: 100 + 50 and 30 + 20
    assertEquals(0, new BigDecimal("150").compareTo(trend.get(1).getIncome()));
    assertEquals(0, new BigDecimal("50").compareTo(trend.get(1).getExpense()));
    assertEquals("AUGUST", trend.get(2).getTitle());
  }

  /**
   * Builds a period total the same way the service does, so derived fields (income, expense) are real.
  */
  private PeriodTotalsDto totals(String realizedIncome, String pendingIncome, String realizedExpense, String pendingExpense) {
    PeriodTotalsDto totals = periodFactory.createEmptyPeriodTotals(PERIOD, LocalDate.of(2026, 6, 15));
    totals.setRealizedIncome(new BigDecimal(realizedIncome));
    totals.setPendingIncome(new BigDecimal(pendingIncome));
    totals.setRealizedExpense(new BigDecimal(realizedExpense));
    totals.setPendingExpense(new BigDecimal(pendingExpense));
    totals.setIncome(totals.getRealizedIncome().add(totals.getPendingIncome()));
    totals.setExpense(totals.getRealizedExpense().add(totals.getPendingExpense()));

    return totals;
  }
}
