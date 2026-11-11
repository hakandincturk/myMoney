package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.dashboard.request.TagSummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.IncomingInstallmentsResponseDto;
import com.hakandincturk.dtos.dashboard.response.LastTransactionsResponseDto;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.dtos.dashboard.response.TagSummaryResponseDto;
import com.hakandincturk.mappers.InstallmentMapper;
import com.hakandincturk.mappers.TransactionMapper;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.models.Tag;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;
import com.hakandincturk.factories.ReportPeriodFactory;
import com.hakandincturk.factories.DashboardSummaryFactory;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;
import com.hakandincturk.services.impl.DashboardServiceImpl;
import com.hakandincturk.services.rules.DashboardRules;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @InjectMocks
  private DashboardServiceImpl dashboardService;

  @Mock
  private DashboardRules dashboardRules;
  @Mock
  private AccountRepository accountRepository;
  @Mock
  private TransactionRepository transactionRepository;
  @Mock
  private InstallmentRepository installmentRepository;
  // Real factories: the live calculation is verified end to end, only the queries are mocked
  @Spy
  private ReportPeriodFactory reportPeriodFactory = new ReportPeriodFactory();
  @Spy
  private DashboardSummaryFactory dashboardSummaryFactory = new DashboardSummaryFactory();
  @Mock
  private TransactionMapper transactionMapper;
  @Mock
  private InstallmentMapper installmentMapper;

  @Test
  @DisplayName("QuickView - hiç taksit yokken sıfır değerler döndürülmeli")
  void quickViewResponse_shouldReturnZeros_whenNoInstallments() {
    Long userId = 1L;

    when(accountRepository.sumBalanceByUserIdAndTypes(eq(userId), anyList()))
        .thenReturn(BigDecimal.valueOf(10000));
    when(transactionRepository.findWaitingTransactionCount(eq(userId), anyList(), anyList()))
        .thenReturn(0L);

    QuickViewResponseDto result = dashboardService.quickViewResponse(userId);

    assertNotNull(result);
    assertEquals(BigDecimal.valueOf(10000), result.getTotalBalance());
    assertEquals(0, BigDecimal.ZERO.compareTo(result.getIncome().getOccured()));
    assertEquals(0, BigDecimal.ZERO.compareTo(result.getExpense().getOccured()));
    assertEquals(0, BigDecimal.ZERO.compareTo(result.getIncome().getWaiting()));
    // Nothing was due last month either, so there is no base for a change rate
    assertNull(result.getIncome().getLastMonthChangeRate());
    assertEquals(0, result.getWaitingInstallments());
  }

  @Test
  @DisplayName("QuickView - taksitlerden canlı hesaplanmalı: bekleyen vade ayına, gerçekleşen ödeme ayına göre")
  void quickViewResponse_shouldCalculateLiveFromInstallments() {
    Long userId = 1L;
    YearMonth current = YearMonth.now();
    YearMonth previous = current.minusMonths(1);

    when(accountRepository.sumBalanceByUserIdAndTypes(eq(userId), anyList()))
        .thenReturn(BigDecimal.valueOf(10000));
    // Due this month: income 5000 paid + 2000 waiting, expense 3000 paid + 1000 waiting
    // Due last month: income 6000, expense 1500 paid + 500 still unpaid
    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(userId, previous.atDay(1), current.atEndOfMonth(), InstallmentStatuses.SKIPPED))
        .thenReturn(List.of(
            amount(current, TransactionTypes.CREDIT, true, 5000),
            amount(current, TransactionTypes.CREDIT, false, 2000),
            amount(current, TransactionTypes.DEBT, true, 3000),
            amount(current, TransactionTypes.DEBT, false, 1000),
            amount(previous, TransactionTypes.CREDIT, true, 6000),
            amount(previous, TransactionTypes.DEBT, true, 1500),
            amount(previous, TransactionTypes.DEBT, false, 500)
        ));
    // Paid this month: income 4000, expense 2500
    when(installmentRepository.sumPaidAmountsByPaidMonthAndType(userId, current.atDay(1), current.atEndOfMonth(), InstallmentStatuses.SKIPPED))
        .thenReturn(List.of(
            amount(current, TransactionTypes.CREDIT, true, 4000),
            amount(current, TransactionTypes.DEBT, true, 2500)
        ));
    when(transactionRepository.findWaitingTransactionCount(eq(userId), anyList(), anyList()))
        .thenReturn(5L);

    QuickViewResponseDto result = dashboardService.quickViewResponse(userId);

    assertEquals(0, BigDecimal.valueOf(2000).compareTo(result.getIncome().getWaiting()));
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(result.getExpense().getWaiting()));
    assertEquals(0, BigDecimal.valueOf(4000).compareTo(result.getIncome().getOccured()));
    assertEquals(0, BigDecimal.valueOf(2500).compareTo(result.getExpense().getOccured()));
    assertEquals(5, result.getWaitingInstallments());

    // Due totals vs. due totals: (7000 - 6000) / 6000 and (4000 - 2000) / 2000
    assertEquals(16.67, result.getIncome().getLastMonthChangeRate());
    assertEquals(100.0, result.getExpense().getLastMonthChangeRate());

    // Tasarruf oranı hem gerçekleşen hem bekleyen gideri kapsar: (6000 - 3500) / 6000 * 100
    assertEquals(41.67, result.getSavingRate());
  }

  @Test
  @DisplayName("QuickView - bu ay vadesi olan taksit yokken bu ay yapılan ödemeler gösterilmeli")
  void quickViewResponse_shouldShowPaymentsMadeThisMonth_whenNothingIsDueThisMonth() {
    Long userId = 1L;
    YearMonth current = YearMonth.now();

    when(installmentRepository.sumPaidAmountsByPaidMonthAndType(eq(userId), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(amount(current, TransactionTypes.DEBT, true, 750)));
    when(transactionRepository.findWaitingTransactionCount(eq(userId), anyList(), anyList()))
        .thenReturn(0L);

    QuickViewResponseDto result = dashboardService.quickViewResponse(userId);

    // The old cache showed 0 here because the month had no summary row
    assertEquals(0, BigDecimal.valueOf(750).compareTo(result.getExpense().getOccured()));
  }

  @Test
  @DisplayName("Son işlemler listesi döndürülmeli")
  void lastTransactions_shouldReturnMappedTransactions() {
    Long userId = 1L;

    Transaction t1 = new Transaction();
    t1.setId(1L);
    Transaction t2 = new Transaction();
    t2.setId(2L);

    when(transactionRepository.findTop10ByUserIdAndIsRemovedFalseOrderByIdDesc(userId))
        .thenReturn(List.of(t1, t2));

    LastTransactionsResponseDto result = dashboardService.lastTransactions(userId);

    assertNotNull(result);
    verify(transactionMapper, times(2)).toLastTransactionDataDto(any());
  }

  @Test
  @DisplayName("Yaklaşan taksitler listesi döndürülmeli")
  void incomingInstallments_shouldReturnMappedInstallments() {
    Long userId = 1L;

    when(installmentRepository.findTop10ByTransaction_UserIdAndDebtDateBetweenAndIsPaidFalseAndIsRemovedFalseOrderByDebtDate(
        eq(userId), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    IncomingInstallmentsResponseDto result = dashboardService.incomingInstallments(userId);

    assertNotNull(result);
    assertNotNull(result.getIncomingInstallmentsDatas());
  }

  @Test
  @DisplayName("Aylık trend - boş veri olduğunda 12 ay sıfır değerlerle dönmeli")
  void monthlyTrend_shouldReturnZeroValues_whenNoData() {
    Long userId = 1L;

    MonthlyTrendResponseDto result = dashboardService.monthlyTrend(userId);

    assertEquals(12, result.getMonthlyTrendData().size());
    result.getMonthlyTrendData().forEach(data -> {
      assertEquals(0, BigDecimal.ZERO.compareTo(data.getIncome()));
      assertEquals(0, BigDecimal.ZERO.compareTo(data.getExpense()));
    });
  }

  @Test
  @DisplayName("Aylık trend - tüm pencere tek sorguyla çekilmeli, aylar vade ayına göre dolmalı")
  void monthlyTrend_shouldQueryWholeWindowOnce() {
    Long userId = 1L;
    YearMonth current = YearMonth.now();
    YearMonth first = current.minusMonths(6);
    YearMonth last = current.plusMonths(5);

    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(userId, first.atDay(1), last.atEndOfMonth(), InstallmentStatuses.SKIPPED))
        .thenReturn(List.of(
            amount(current, TransactionTypes.CREDIT, true, 1000),
            amount(current, TransactionTypes.CREDIT, false, 500),
            amount(last, TransactionTypes.DEBT, false, 1234)
        ));

    MonthlyTrendResponseDto result = dashboardService.monthlyTrend(userId);

    verify(installmentRepository, times(1)).sumMonthlyAmountsByTypeAndPaidState(eq(userId), any(), any(), any());
    assertEquals(first.getMonth().toString(), result.getMonthlyTrendData().get(0).getTitle());
    assertEquals(0, BigDecimal.valueOf(1500).compareTo(result.getMonthlyTrendData().get(6).getIncome()));
    assertEquals(0, BigDecimal.valueOf(1234).compareTo(result.getMonthlyTrendData().get(11).getExpense()));
  }

  private MonthlyTypeAmountProjection amount(YearMonth period, TransactionTypes type, boolean paid, int value) {
    return new MonthlyTypeAmountProjection(period.getYear(), period.getMonthValue(), type, paid, BigDecimal.valueOf(value));
  }

  @Test
  @DisplayName("Tag özeti - etiket olmayan taksitler '-' olarak gruplanmalı")
  void tagSummary_shouldGroupUntaggedAs_dash() {
    Long userId = 1L;
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 6, 1),
        LocalDate.of(2025, 6, 30)
    );

    when(installmentRepository.findInstallmentTagAmounts(
        eq(userId), anyList(), any(LocalDate.class), any(LocalDate.class), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(500), null, null)));

    TagSummaryResponseDto result = dashboardService.tagSummary(userId, DashboardTagSummaryTypes.MONTHLY, DashboardTagSummarySumMode.DOUBLE_COUNT, body);

    assertNotNull(result);
    assertEquals(1, result.getTagSummaryDatas().size());
    assertEquals("-", result.getTagSummaryDatas().get(0).getName());
  }

  @Test
  @DisplayName("Tag özeti - DISTRIBUTED modda tutarlar etiket sayısına bölünmeli")
  void tagSummary_distributed_shouldDivideAmountByTagCount() {
    Long userId = 1L;
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 6, 1),
        LocalDate.of(2025, 6, 30)
    );

    when(installmentRepository.findInstallmentTagAmounts(
        eq(userId), anyList(), any(LocalDate.class), any(LocalDate.class), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(
            new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(1000), 1L, "Yemek"),
            new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(1000), 2L, "Eğlence")
        ));

    TagSummaryResponseDto result = dashboardService.tagSummary(userId, DashboardTagSummaryTypes.MONTHLY, DashboardTagSummarySumMode.DISTRIBUTED, body);

    assertNotNull(result);
    assertEquals(2, result.getTagSummaryDatas().size());
    result.getTagSummaryDatas().forEach(data ->
        assertEquals(0, BigDecimal.valueOf(500).compareTo(data.getAmount()))
    );
  }
}
