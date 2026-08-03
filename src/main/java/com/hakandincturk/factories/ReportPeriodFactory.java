package com.hakandincturk.factories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hakandincturk.dtos.report.response.PeriodTotalsDto;
import com.hakandincturk.repositories.projections.MonthlyEntityCountProjection;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;
import com.hakandincturk.utils.ReportMathUtils;
import com.hakandincturk.utils.ReportPeriodUtils;
import com.hakandincturk.utils.TransactionClassifier;

/**
 * Ham ay kırılımı projeksiyonlarını rapor dönem toplamlarına çevirir.
 * Gelir/gider sınıflandırması burada TransactionClassifier üzerinden tek noktadan yapılır.
 */
@Component
public class ReportPeriodFactory {

  /**
   * Aralıktaki her ay için dönem toplamı üretir; veri olmayan aylar sıfırlı kayıtla doldurulur.
   * @param startPeriod Aralığın ilk ayı
   * @param endPeriod Aralığın son ayı
   * @param amounts Ay + hareket tipi + ödeme durumu kırılımındaki tutarlar
   * @param counts Ay bazlı işlem ve taksit adetleri
   * @param today Dönemin kesinlik seviyesini belirlemek için referans tarih
  */
  public Map<YearMonth, PeriodTotalsDto> buildPeriodTotals(
    YearMonth startPeriod,
    YearMonth endPeriod,
    List<MonthlyTypeAmountProjection> amounts,
    List<MonthlyEntityCountProjection> counts,
    LocalDate today
  ){
    Map<YearMonth, PeriodTotalsDto> periodTotals = new LinkedHashMap<>();
    for (YearMonth period : ReportPeriodUtils.monthsBetween(startPeriod, endPeriod)) {
      periodTotals.put(period, this.createEmptyPeriodTotals(period, today));
    }

    for (MonthlyTypeAmountProjection amount : amounts) {
      PeriodTotalsDto totals = periodTotals.get(YearMonth.of(amount.year(), amount.month()));
      if(totals == null){
        continue;
      }

      this.applyAmount(totals, amount);
    }

    for (MonthlyEntityCountProjection count : counts) {
      PeriodTotalsDto totals = periodTotals.get(YearMonth.of(count.year(), count.month()));
      if(totals == null){
        continue;
      }

      totals.setTransactionCount(Math.toIntExact(count.transactionCount()));
      totals.setInstallmentCount(Math.toIntExact(count.installmentCount()));
    }

    periodTotals.values().forEach(this::applyDerivedTotals);

    return periodTotals;
  }

  /**
   * Hiç hareketi olmayan bir ay için sıfırlı dönem toplamı üretir.
   * @param period Hesaplanacak ay
   * @param today Dönemin kesinlik seviyesini belirlemek için referans tarih
  */
  public PeriodTotalsDto createEmptyPeriodTotals(YearMonth period, LocalDate today){
    PeriodTotalsDto totals = new PeriodTotalsDto();
    totals.setYear(period.getYear());
    totals.setMonth(period.getMonthValue());
    totals.setLabel(ReportPeriodUtils.labelOf(period));
    totals.setKind(ReportPeriodUtils.kindOf(period, today));
    totals.setIncome(ReportMathUtils.money(BigDecimal.ZERO));
    totals.setExpense(ReportMathUtils.money(BigDecimal.ZERO));
    totals.setNet(ReportMathUtils.money(BigDecimal.ZERO));
    totals.setRealizedIncome(ReportMathUtils.money(BigDecimal.ZERO));
    totals.setPendingIncome(ReportMathUtils.money(BigDecimal.ZERO));
    totals.setRealizedExpense(ReportMathUtils.money(BigDecimal.ZERO));
    totals.setPendingExpense(ReportMathUtils.money(BigDecimal.ZERO));
    totals.setTransactionCount(0);
    totals.setInstallmentCount(0);

    return totals;
  }

  /**
   * Tek bir kırılım satırını ilgili gerçekleşen/bekleyen kovasına ekler.
  */
  private void applyAmount(PeriodTotalsDto totals, MonthlyTypeAmountProjection projection){
    BigDecimal amount = ReportMathUtils.zeroIfNull(projection.amount());
    boolean paid = Boolean.TRUE.equals(projection.paid());

    if(TransactionClassifier.isIncome(projection.type())){
      if(paid){
        totals.setRealizedIncome(totals.getRealizedIncome().add(amount));
      }
      else {
        totals.setPendingIncome(totals.getPendingIncome().add(amount));
      }

      return;
    }

    if(TransactionClassifier.isExpense(projection.type())){
      if(paid){
        totals.setRealizedExpense(totals.getRealizedExpense().add(amount));
      }
      else {
        totals.setPendingExpense(totals.getPendingExpense().add(amount));
      }
    }
  }

  /**
   * Türetilmiş alanları hesaplar ve tüm para alanlarını 2 ondalığa sabitler.
  */
  private void applyDerivedTotals(PeriodTotalsDto totals){
    totals.setRealizedIncome(ReportMathUtils.money(totals.getRealizedIncome()));
    totals.setPendingIncome(ReportMathUtils.money(totals.getPendingIncome()));
    totals.setRealizedExpense(ReportMathUtils.money(totals.getRealizedExpense()));
    totals.setPendingExpense(ReportMathUtils.money(totals.getPendingExpense()));

    BigDecimal income = totals.getRealizedIncome().add(totals.getPendingIncome());
    BigDecimal expense = totals.getRealizedExpense().add(totals.getPendingExpense());

    totals.setIncome(ReportMathUtils.money(income));
    totals.setExpense(ReportMathUtils.money(expense));
    totals.setNet(ReportMathUtils.money(income.subtract(expense)));
  }

}
