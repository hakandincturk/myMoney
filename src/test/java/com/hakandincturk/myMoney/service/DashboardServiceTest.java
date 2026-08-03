package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.core.enums.MonthlySummeryTypes;
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
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.models.Tag;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.TransactionRepository;
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
  @Mock
  private MonthlySummaryRepository monthlySummaryRepository;
  @Mock
  private TransactionMapper transactionMapper;
  @Mock
  private InstallmentMapper installmentMapper;

  @Test
  @DisplayName("QuickView - aylık özet olmadığında sıfır değerler döndürülmeli")
  void quickViewResponse_shouldReturnZeros_whenNoMonthlySummary() {
    Long userId = 1L;

    when(accountRepository.sumBalanceByUserIdAndTypes(eq(userId), anyList()))
        .thenReturn(BigDecimal.valueOf(10000));
    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        eq(userId), anyInt(), anyInt(), eq(MonthlySummeryTypes.TRANSACTION)))
        .thenReturn(Optional.empty());
    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        eq(userId), anyInt(), anyInt(), eq(MonthlySummeryTypes.PAYMENT)))
        .thenReturn(Optional.empty());
    when(transactionRepository.findWaitingTransactionCount(eq(userId), anyList(), anyList()))
        .thenReturn(0L);

    QuickViewResponseDto result = dashboardService.quickViewResponse(userId);

    assertNotNull(result);
    assertEquals(BigDecimal.valueOf(10000), result.getTotalBalance());
    assertEquals(BigDecimal.ZERO, result.getIncome().getOccured());
    assertEquals(BigDecimal.ZERO, result.getExpense().getOccured());
    assertEquals(0, result.getWaitingInstallments());
  }

  @Test
  @DisplayName("QuickView - aylık özet varken değerler doğru hesaplanmalı")
  void quickViewResponse_shouldCalculateCorrectly_whenMonthlySummaryExists() {
    Long userId = 1L;

    MonthlySummary transactionSummary = new MonthlySummary();
    transactionSummary.setTotalIncome(BigDecimal.valueOf(5000));
    transactionSummary.setTotalExpense(BigDecimal.valueOf(3000));
    transactionSummary.setTotalWaitingIncome(BigDecimal.valueOf(2000));
    transactionSummary.setTotalWaitingExpense(BigDecimal.valueOf(1000));

    MonthlySummary paymentSummary = new MonthlySummary();
    paymentSummary.setTotalIncome(BigDecimal.valueOf(4000));
    paymentSummary.setTotalExpense(BigDecimal.valueOf(2500));

    when(accountRepository.sumBalanceByUserIdAndTypes(eq(userId), anyList()))
        .thenReturn(BigDecimal.valueOf(10000));
    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        eq(userId), anyInt(), anyInt(), eq(MonthlySummeryTypes.TRANSACTION)))
        .thenReturn(Optional.of(transactionSummary));
    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        eq(userId), anyInt(), anyInt(), eq(MonthlySummeryTypes.PAYMENT)))
        .thenReturn(Optional.of(paymentSummary));
    when(transactionRepository.findWaitingTransactionCount(eq(userId), anyList(), anyList()))
        .thenReturn(5L);

    QuickViewResponseDto result = dashboardService.quickViewResponse(userId);

    assertNotNull(result);
    assertEquals(BigDecimal.valueOf(2000), result.getIncome().getWaiting());
    assertEquals(BigDecimal.valueOf(1000), result.getExpense().getWaiting());
    assertEquals(BigDecimal.valueOf(4000), result.getIncome().getOccured());
    assertEquals(BigDecimal.valueOf(2500), result.getExpense().getOccured());
    assertEquals(5, result.getWaitingInstallments());

    // Tasarruf oranı hem gerçekleşen hem bekleyen gideri kapsar: (6000 - 3500) / 6000 * 100
    assertEquals(41.67, result.getSavingRate());
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
  @DisplayName("Aylık trend - boş veri olduğunda sıfır değerlerle dönmeli")
  void monthlyTrend_shouldReturnZeroValues_whenNoData() {
    Long userId = 1L;

    when(monthlySummaryRepository.findByUser_IdAndSummaryDateBetweenAndTypeAndIsRemovedFalse(
        eq(userId), any(LocalDate.class), any(LocalDate.class), eq(MonthlySummeryTypes.TRANSACTION)))
        .thenReturn(List.of());

    MonthlyTrendResponseDto result = dashboardService.monthlyTrend(userId);

    assertNotNull(result);
    assertNotNull(result.getMonthlyTrendData());
    assertTrue(result.getMonthlyTrendData().size() > 0);
    result.getMonthlyTrendData().forEach(data -> {
      assertEquals(BigDecimal.ZERO, data.getIncome());
      assertEquals(BigDecimal.ZERO, data.getExpense());
    });
  }

  @Test
  @DisplayName("Tag özeti - etiket olmayan taksitler '-' olarak gruplanmalı")
  void tagSummary_shouldGroupUntaggedAs_dash() {
    Long userId = 1L;
    TagSummaryRequestDto body = new TagSummaryRequestDto(
        LocalDate.of(2025, 6, 1),
        LocalDate.of(2025, 6, 30)
    );

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.DEBT);
    transaction.setTransactionTags(List.of());

    Installment installment = new Installment();
    installment.setAmount(BigDecimal.valueOf(500));
    installment.setTransaction(transaction);

    when(installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(
        eq(userId), anyList(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(installment));

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

    Tag tag1 = new Tag();
    tag1.setName("Yemek");
    Tag tag2 = new Tag();
    tag2.setName("Eğlence");

    TransactionTag tt1 = new TransactionTag();
    tt1.setTag(tag1);
    TransactionTag tt2 = new TransactionTag();
    tt2.setTag(tag2);

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.DEBT);
    transaction.setTransactionTags(List.of(tt1, tt2));

    Installment installment = new Installment();
    installment.setAmount(BigDecimal.valueOf(1000));
    installment.setTransaction(transaction);

    when(installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(
        eq(userId), anyList(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(installment));

    TagSummaryResponseDto result = dashboardService.tagSummary(userId, DashboardTagSummaryTypes.MONTHLY, DashboardTagSummarySumMode.DISTRIBUTED, body);

    assertNotNull(result);
    assertEquals(2, result.getTagSummaryDatas().size());
    result.getTagSummaryDatas().forEach(data ->
        assertEquals(0, BigDecimal.valueOf(500).compareTo(data.getAmount()))
    );
  }
}
