package com.hakandincturk.factories;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hakandincturk.dtos.dashboard.response.MonthlyTrendDataDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewIncomeAndExpenseDetailDto;
import com.hakandincturk.dtos.report.response.PeriodTotalsDto;
import com.hakandincturk.utils.ReportMathUtils;
import com.hakandincturk.utils.ReportPeriodUtils;

/**
 * Builds the home page figures from live period totals instead of the monthly_summary table,
 * so they can never go stale (e.g. after a transaction is deleted).
 *
 * Two bases are used:
 * - by debt date: what falls due in a month (income/expense, of which paid vs. still waiting)
 * - by paid date: what was actually paid in a month, whatever its due date
 * Both come from ReportPeriodFactory, so the home page and the reports use the same numbers.
 * The month-over-month change compares due totals with due totals, exactly like the reports' deltaVsPrevious.
 */
@Component
public class DashboardSummaryFactory {

  /**
   * @param currentByDebtDate Current month by due date
   * @param previousByDebtDate Previous month by due date, the base of the change rate
   * @param currentByPaidDate Current month by paid date
  */
  public QuickViewIncomeAndExpenseDetailDto buildIncomeDetail(PeriodTotalsDto currentByDebtDate, PeriodTotalsDto previousByDebtDate, PeriodTotalsDto currentByPaidDate){
    return this.buildDetail(
      currentByDebtDate.getPendingIncome(),
      currentByPaidDate.getIncome(),
      ReportMathUtils.changeRate(currentByDebtDate.getIncome(), previousByDebtDate.getIncome())
    );
  }

  /**
   * @param currentByDebtDate Current month by due date
   * @param previousByDebtDate Previous month by due date, the base of the change rate
   * @param currentByPaidDate Current month by paid date
  */
  public QuickViewIncomeAndExpenseDetailDto buildExpenseDetail(PeriodTotalsDto currentByDebtDate, PeriodTotalsDto previousByDebtDate, PeriodTotalsDto currentByPaidDate){
    return this.buildDetail(
      currentByDebtDate.getPendingExpense(),
      currentByPaidDate.getExpense(),
      ReportMathUtils.changeRate(currentByDebtDate.getExpense(), previousByDebtDate.getExpense())
    );
  }

  /**
   * One point per month from firstPeriod to lastPeriod (both inclusive); a month without data is 0.
   * Income and expense are the whole month by due date, paid and still waiting together.
  */
  public List<MonthlyTrendDataDto> buildTrend(Map<YearMonth, PeriodTotalsDto> totalsByDebtDate, YearMonth firstPeriod, YearMonth lastPeriod){
    List<MonthlyTrendDataDto> trend = new ArrayList<>();
    for (YearMonth period : ReportPeriodUtils.monthsBetween(firstPeriod, lastPeriod)) {
      PeriodTotalsDto totals = totalsByDebtDate.get(period);

      MonthlyTrendDataDto point = new MonthlyTrendDataDto();
      point.setTitle(period.getMonth().toString());
      point.setIncome(totals != null ? totals.getIncome() : BigDecimal.ZERO);
      point.setExpense(totals != null ? totals.getExpense() : BigDecimal.ZERO);
      trend.add(point);
    }

    return trend;
  }

  /**
   * @param waiting Due this month and not paid yet
   * @param occurred Paid this month
   * @param changeRate Month-over-month change of the due total; null when last month had nothing due
  */
  private QuickViewIncomeAndExpenseDetailDto buildDetail(BigDecimal waiting, BigDecimal occurred, Double changeRate){
    QuickViewIncomeAndExpenseDetailDto detail = new QuickViewIncomeAndExpenseDetailDto();
    detail.setWaiting(waiting);
    detail.setOccured(occurred);
    detail.setLastMonthChangeRate(changeRate);

    return detail;
  }

}
