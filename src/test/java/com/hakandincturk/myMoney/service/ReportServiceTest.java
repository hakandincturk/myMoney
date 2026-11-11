package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.PeriodKind;
import com.hakandincturk.core.enums.RecurringKinds;
import com.hakandincturk.core.enums.ReportFlowTypes;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.report.request.ReportRecurringRequestDto;
import com.hakandincturk.dtos.report.request.ReportSummaryRequestDto;
import com.hakandincturk.dtos.report.request.ReportTagBreakdownRequestDto;
import com.hakandincturk.dtos.report.request.ReportTimelineRequestDto;
import com.hakandincturk.dtos.report.request.ReportTopExpensesRequestDto;
import com.hakandincturk.dtos.report.response.RecurringItemDto;
import com.hakandincturk.dtos.report.response.RecurringMonthAmountDto;
import com.hakandincturk.dtos.report.response.ReportRecurringResponseDto;
import com.hakandincturk.dtos.report.response.ReportSummaryResponseDto;
import com.hakandincturk.dtos.report.response.ReportTagBreakdownResponseDto;
import com.hakandincturk.dtos.report.response.ReportTimelineResponseDto;
import com.hakandincturk.dtos.report.response.ReportTopExpensesResponseDto;
import com.hakandincturk.dtos.report.response.TimelinePointDto;
import com.hakandincturk.dtos.report.response.TopExpenseItemDto;
import com.hakandincturk.factories.ReportPeriodFactory;
import com.hakandincturk.factories.ReportRecurringFactory;
import com.hakandincturk.factories.ReportTagFactory;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;
import com.hakandincturk.repositories.projections.MonthlyAmountProjection;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;
import com.hakandincturk.repositories.projections.RecurringInstallmentProjection;
import com.hakandincturk.repositories.projections.TopInstallmentProjection;
import com.hakandincturk.repositories.projections.TransactionNextDueProjection;
import com.hakandincturk.repositories.projections.TransactionTagProjection;
import com.hakandincturk.services.impl.ReportServiceImpl;
import com.hakandincturk.services.rules.ReportRules;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @InjectMocks
  private ReportServiceImpl reportService;

  @Mock
  private ReportRules reportRules;
  @Mock
  private InstallmentRepository installmentRepository;
  @Mock
  private TransactionTagRepository transactionTagRepository;

  // Hesaplama davranışını uçtan uca doğrulamak için factory'ler gerçek nesnelerdir
  @Spy
  private ReportPeriodFactory reportPeriodFactory = new ReportPeriodFactory();
  @Spy
  private ReportTagFactory reportTagFactory = new ReportTagFactory();
  @Spy
  private ReportRecurringFactory reportRecurringFactory = new ReportRecurringFactory();

  private static final Long USER_ID = 1L;
  private static final Long OTHER_USER_ID = 2L;

  @Test
  @DisplayName("Özet - önceki, seçili ve sonraki ay toplamlarıyla farkları hesaplanmalı")
  void summary_shouldCalculateTotalsAndDeltas() {
    YearMonth current = YearMonth.now();
    YearMonth previous = current.minusMonths(1);

    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(eq(USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(
            amount(previous, TransactionTypes.CREDIT, true, 1000),
            amount(current, TransactionTypes.CREDIT, true, 2000),
            amount(current, TransactionTypes.DEBT, false, 500)
        ));

    ReportSummaryResponseDto response = reportService.summary(USER_ID, summaryRequest(current));

    assertEquals(PeriodKind.ACTUAL, response.getPrevious().getKind());
    assertEquals(PeriodKind.PARTIAL, response.getCurrent().getKind());
    assertEquals(PeriodKind.PROJECTED, response.getNext().getKind());

    assertEquals(0, BigDecimal.valueOf(2000).compareTo(response.getCurrent().getIncome()));
    assertEquals(0, BigDecimal.valueOf(500).compareTo(response.getCurrent().getExpense()));
    assertEquals(0, BigDecimal.valueOf(1500).compareTo(response.getCurrent().getNet()));

    // previous -> current geçişi, payda previous
    assertEquals(100.00, response.getDeltaVsPrevious().getIncomeChangeRate());
    assertNull(response.getDeltaVsPrevious().getExpenseChangeRate());
    assertEquals(0, BigDecimal.valueOf(500).compareTo(response.getDeltaVsPrevious().getNetChangeAmount()));

    // current -> next geçişi, payda current; next sıfır olduğu için oran -100
    assertEquals(-100.00, response.getDeltaVsNext().getIncomeChangeRate());
    assertEquals(-100.00, response.getDeltaVsNext().getExpenseChangeRate());
    assertEquals(0, BigDecimal.valueOf(-1500).compareTo(response.getDeltaVsNext().getNetChangeAmount()));

    assertEquals(75.00, response.getSavingRate());
    assertNull(response.getBusiestDay());
    assertNull(response.getTopTag());
  }

  @Test
  @DisplayName("Özet - günlük ortalama yalnız gerçekleşen giderin geçen güne bölümü olmalı")
  void summary_shouldAverageOnlyRealizedExpense_forCurrentMonth() {
    YearMonth current = YearMonth.now();
    LocalDate today = LocalDate.now();

    // 3000 ödendi, 12000 ayın ilerisinde planlı; planlı kısım ortalamaya girmemeli
    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(eq(USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(
            amount(current, TransactionTypes.DEBT, true, 3000),
            amount(current, TransactionTypes.DEBT, false, 12000)
        ));

    ReportSummaryResponseDto response = reportService.summary(USER_ID, summaryRequest(current));

    assertEquals(current.lengthOfMonth(), response.getDaysInMonth());
    assertEquals(today.getDayOfMonth(), response.getDaysElapsed());
    // Ayın toplamı zaten biliniyor, ayrıca tahmin edilmez
    assertEquals(0, BigDecimal.valueOf(15000).compareTo(response.getCurrent().getExpense()));

    BigDecimal expectedAverage = BigDecimal.valueOf(3000)
        .divide(BigDecimal.valueOf(today.getDayOfMonth()), 2, RoundingMode.HALF_UP);
    assertEquals(0, expectedAverage.compareTo(response.getAverageDailyExpense()));
  }

  @Test
  @DisplayName("Özet - geçmiş ayda günlük ortalama ayın tüm günlerine bölünmeli")
  void summary_shouldDivideByWholeMonth_forPastMonth() {
    YearMonth past = YearMonth.of(2026, 7);

    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(eq(USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(amount(past, TransactionTypes.DEBT, true, 23499)));

    ReportSummaryResponseDto response = reportService.summary(USER_ID, summaryRequest(past));

    assertEquals(31, response.getDaysInMonth());
    assertEquals(31, response.getDaysElapsed());
    // 23499 / 31 = 758.03
    assertEquals(0, new BigDecimal("758.03").compareTo(response.getAverageDailyExpense()));
  }

  @Test
  @DisplayName("Özet - gelecek ayda henüz gerçekleşen gider olmadığı için günlük ortalama 0 olmalı")
  void summary_shouldReturnZeroAverage_forProjectedMonth() {
    YearMonth future = YearMonth.now().plusMonths(2);

    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(eq(USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(amount(future, TransactionTypes.DEBT, false, 5000)));

    ReportSummaryResponseDto response = reportService.summary(USER_ID, summaryRequest(future));

    assertEquals(PeriodKind.PROJECTED, response.getCurrent().getKind());
    assertEquals(0, BigDecimal.valueOf(5000).compareTo(response.getCurrent().getExpense()));
    assertEquals(0, BigDecimal.ZERO.compareTo(response.getAverageDailyExpense()));
  }

  @Test
  @DisplayName("Özet - veri olmayan aylar için sıfırlı dönem dönmeli")
  void summary_shouldReturnZeroPeriods_whenUserHasNoData() {
    ReportSummaryResponseDto response = reportService.summary(USER_ID, summaryRequest(YearMonth.now()));

    assertNotNull(response.getPrevious());
    assertNotNull(response.getNext());
    assertEquals(0, BigDecimal.ZERO.compareTo(response.getCurrent().getIncome()));
    assertEquals(0.0, response.getSavingRate());
    assertEquals(0, response.getCurrent().getTransactionCount());
  }

  @Test
  @DisplayName("Özet - dönem doğrulaması kural sınıfına devredilmeli")
  void summary_shouldDelegateValidationToRules() {
    YearMonth current = YearMonth.now();

    reportService.summary(USER_ID, summaryRequest(current));

    verify(reportRules).checkPeriodIsValid(current.getYear(), current.getMonthValue());
  }

  @Test
  @DisplayName("Tüm sorgular oturumdaki kullanıcıya ve aktif taksitlere kısıtlanmalı")
  void summary_shouldScopeQueriesToUserAndSkipSkippedInstallments() {
    reportService.summary(OTHER_USER_ID, summaryRequest(YearMonth.now()));

    verify(installmentRepository).sumMonthlyAmountsByTypeAndPaidState(eq(OTHER_USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED));
    verify(installmentRepository).countMonthlyEntities(eq(OTHER_USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED));
    verify(installmentRepository).sumDailyAmounts(eq(OTHER_USER_ID), anyList(), any(), any(), eq(InstallmentStatuses.SKIPPED));
    verify(installmentRepository).findInstallmentTagAmounts(eq(OTHER_USER_ID), anyList(), any(), any(), eq(InstallmentStatuses.SKIPPED));
    verifyNoMoreInteractions(installmentRepository);
  }

  @Test
  @DisplayName("Zaman serisi - veri olmayan aylar sıfırlı kayıtla doldurulmalı")
  void timeline_shouldFillMissingMonthsWithZeroPoints() {
    YearMonth center = YearMonth.of(2026, 6);

    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(eq(USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(amount(center, TransactionTypes.DEBT, true, 900)));

    ReportTimelineResponseDto response = reportService.timeline(USER_ID, timelineRequest(center, 2, 2));

    List<TimelinePointDto> points = response.getPoints();
    assertEquals(5, points.size());
    assertEquals(2026, points.get(0).getYear());
    assertEquals(4, points.get(0).getMonth());
    assertEquals("APRIL", points.get(0).getLabel());
    assertEquals(0, BigDecimal.ZERO.compareTo(points.get(0).getExpense()));
    assertEquals(0, BigDecimal.valueOf(900).compareTo(points.get(2).getExpense()));
    assertEquals(8, points.get(4).getMonth());
  }

  @Test
  @DisplayName("Zaman serisi - ay sonundaki kalan borç geçmişe doğru geri hesaplanmalı")
  void timeline_shouldCalculateCumulativeRemainingDebt() {
    YearMonth center = YearMonth.of(2026, 6);

    when(installmentRepository.sumUnpaidAmount(eq(USER_ID), anyList(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(BigDecimal.valueOf(1000));
    when(installmentRepository.sumPaidAmountsByPaidMonth(eq(USER_ID), anyList(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(
            new MonthlyAmountProjection(2026, 5, BigDecimal.valueOf(200)),
            new MonthlyAmountProjection(2026, 6, BigDecimal.valueOf(300))
        ));

    ReportTimelineResponseDto response = reportService.timeline(USER_ID, timelineRequest(center, 1, 1));

    List<TimelinePointDto> points = response.getPoints();
    assertEquals(3, points.size());
    // Mayıs sonunda haziranda ödenen 300 halen borçtur
    assertEquals(0, BigDecimal.valueOf(1300).compareTo(points.get(0).getCumulativeRemainingDebt()));
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(points.get(1).getCumulativeRemainingDebt()));
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(points.get(2).getCumulativeRemainingDebt()));
  }

  @Test
  @DisplayName("Zaman serisi - projeksiyon borcu ay sonundan sonraki vadelerden hesaplanmalı")
  void timeline_shouldCalculateProjectedRemainingDebt() {
    YearMonth center = YearMonth.of(2026, 6);

    when(installmentRepository.sumUnpaidAmountsByDebtMonth(eq(USER_ID), anyList(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(
            new MonthlyAmountProjection(2026, 6, BigDecimal.valueOf(400)),
            new MonthlyAmountProjection(2026, 7, BigDecimal.valueOf(600)),
            new MonthlyAmountProjection(2026, 9, BigDecimal.valueOf(250))
        ));

    ReportTimelineResponseDto response = reportService.timeline(USER_ID, timelineRequest(center, 1, 1));

    List<TimelinePointDto> points = response.getPoints();
    // Mayıs sonunda önde 400 + 600 + 250, Haziran sonunda 600 + 250, Temmuz sonunda pencere dışı 250
    assertEquals(0, BigDecimal.valueOf(1250).compareTo(points.get(0).getProjectedRemainingDebt()));
    assertEquals(0, BigDecimal.valueOf(850).compareTo(points.get(1).getProjectedRemainingDebt()));
    assertEquals(0, BigDecimal.valueOf(250).compareTo(points.get(2).getProjectedRemainingDebt()));
  }

  @Test
  @DisplayName("Zaman serisi - çok taksitli işlemde projeksiyon borcu azalmalı, kalan borç gelecekte sabit kalmalı")
  void timeline_shouldDecreaseProjectedDebt_whileCumulativeDebtStaysFlat() {
    YearMonth center = YearMonth.of(2026, 6);
    YearMonth firstDue = YearMonth.of(2026, 7);
    YearMonth lastDue = firstDue.plusMonths(8);

    // 12 taksitli işlemin kalan 9 taksiti: Temmuz 2026 - Mart 2027, her biri 1000, hiçbiri ödenmemiş
    List<MonthlyAmountProjection> futureInstallments = new ArrayList<>();
    for (YearMonth month = firstDue; !month.isAfter(lastDue); month = month.plusMonths(1)) {
      futureInstallments.add(new MonthlyAmountProjection(month.getYear(), month.getMonthValue(), BigDecimal.valueOf(1000)));
    }
    when(installmentRepository.sumUnpaidAmountsByDebtMonth(eq(USER_ID), anyList(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(futureInstallments);
    when(installmentRepository.sumUnpaidAmount(eq(USER_ID), anyList(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(BigDecimal.valueOf(9000));

    ReportTimelineResponseDto response = reportService.timeline(USER_ID, timelineRequest(center, 0, 12));

    List<TimelinePointDto> points = response.getPoints();
    assertEquals(13, points.size());

    // Haziran sonu 9000, her ay bir taksit düşer, Mart 2027 sonunda 0
    for (int index = 0; index <= 9; index++) {
      BigDecimal expected = BigDecimal.valueOf(9000 - index * 1000L);
      assertEquals(0, expected.compareTo(points.get(index).getProjectedRemainingDebt()), "index " + index);
      if (index > 0) {
        assertTrue(points.get(index).getProjectedRemainingDebt().compareTo(points.get(index - 1).getProjectedRemainingDebt()) < 0);
      }
    }
    // Son taksitin ayından sonra 0'da kalır
    for (int index = 10; index < points.size(); index++) {
      assertEquals(0, BigDecimal.ZERO.compareTo(points.get(index).getProjectedRemainingDebt()));
    }

    // Aynı seride geriye bakan metrik gelecekte ödeme olmadığı için düz gider
    points.forEach(point -> assertEquals(0, BigDecimal.valueOf(9000).compareTo(point.getCumulativeRemainingDebt())));
  }

  @Test
  @DisplayName("Zaman serisi - ay sayıları 24 ile sınırlanmalı, negatifler default'a düşmeli")
  void timeline_shouldClampMonthParameters() {
    YearMonth center = YearMonth.of(2026, 6);

    ReportTimelineResponseDto response = reportService.timeline(USER_ID, timelineRequest(center, 100, -5));

    // 24 geçmiş ay + içinde bulunulan ay + 6 (default) gelecek ay
    assertEquals(31, response.getPoints().size());
  }

  @Test
  @DisplayName("Etiket kırılımı - seçili ay ve önceki ay ayrı ayrı sorgulanmalı")
  void tagBreakdown_shouldQueryCurrentAndPreviousMonth() {
    YearMonth current = YearMonth.of(2026, 6);

    when(installmentRepository.findInstallmentTagAmounts(eq(USER_ID), anyList(), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(new InstallmentTagAmountProjection(1L, 10L, BigDecimal.valueOf(400), 3L, "Market")));

    ReportTagBreakdownResponseDto response = reportService.tagBreakdown(USER_ID, tagBreakdownRequest(current));

    ArgumentCaptor<LocalDate> startDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
    verify(installmentRepository, times(2))
        .findInstallmentTagAmounts(eq(USER_ID), anyList(), startDateCaptor.capture(), any(), eq(InstallmentStatuses.SKIPPED));

    assertEquals(LocalDate.of(2026, 6, 1), startDateCaptor.getAllValues().get(0));
    assertEquals(LocalDate.of(2026, 5, 1), startDateCaptor.getAllValues().get(1));
    assertEquals(1, response.getItems().size());
    assertEquals("Market", response.getItems().get(0).getName());
  }

  @Test
  @DisplayName("En büyük kalemler - taksit satırları ve dönem toplamı dönmeli")
  void topExpenses_shouldReturnItemsWithPeriodTotal() {
    YearMonth current = YearMonth.of(2026, 6);

    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(eq(USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(amount(current, TransactionTypes.DEBT, false, 39300)));
    when(installmentRepository.findTopInstallments(eq(USER_ID), anyList(), any(), any(), eq(InstallmentStatuses.SKIPPED), any()))
        .thenReturn(List.of(new TopInstallmentProjection(
            940L, 128L, "Buzdolabı", "Arçelik 580 lt", null,
            BigDecimal.valueOf(12000), LocalDate.of(2026, 6, 15),
            "Garanti Bonus", null, 3, 12, false)));
    when(transactionTagRepository.findTagsOfTransactions(eq(USER_ID), anyList()))
        .thenReturn(List.of(new TransactionTagProjection(128L, 7L, "Ev")));

    ReportTopExpensesResponseDto response = reportService.topExpenses(USER_ID, topExpensesRequest(current));

    assertEquals(1, response.getItems().size());
    TopExpenseItemDto item = response.getItems().get(0);
    assertEquals(128L, item.getTransactionId());
    assertEquals(940L, item.getInstallmentId());
    assertEquals("Buzdolabı", item.getName());
    assertEquals("Arçelik 580 lt", item.getDescription());
    assertEquals(0, BigDecimal.valueOf(12000).compareTo(item.getAmount()));
    assertEquals("Garanti Bonus", item.getAccountName());
    assertNull(item.getContactName());
    assertFalse(item.isPaid());
    assertEquals(1, item.getTags().size());
    assertEquals("Ev", item.getTags().get(0).getName());
    assertEquals(0, BigDecimal.valueOf(39300).compareTo(response.getPeriodTotal()));
  }

  @Test
  @DisplayName("En büyük kalemler - gelir tipinde dönem toplamı gelir olmalı")
  void topExpenses_shouldUseIncomeTotal_whenTypeIsIncome() {
    YearMonth current = YearMonth.of(2026, 6);
    ReportTopExpensesRequestDto request = topExpensesRequest(current);
    request.setType(ReportFlowTypes.INCOME);

    when(installmentRepository.sumMonthlyAmountsByTypeAndPaidState(eq(USER_ID), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(amount(current, TransactionTypes.CREDIT, true, 5000)));

    ReportTopExpensesResponseDto response = reportService.topExpenses(USER_ID, request);

    assertEquals(0, BigDecimal.valueOf(5000).compareTo(response.getPeriodTotal()));
  }

  @Test
  @DisplayName("Tekrar edenler - pencere içinde bulunulan ayı kapsayacak şekilde geriye bakmalı")
  void recurring_shouldLookBackIncludingCurrentMonth() {
    YearMonth current = YearMonth.now();

    when(installmentRepository.findRecurringCandidates(eq(USER_ID), anyList(), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(
            new RecurringInstallmentProjection(1L, "Kredi", 12, BigDecimal.valueOf(500), current.minusMonths(1).atDay(1)),
            new RecurringInstallmentProjection(1L, "Kredi", 12, BigDecimal.valueOf(500), current.atDay(1))
        ));
    when(installmentRepository.findNextDueDates(eq(USER_ID), anyList(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(new TransactionNextDueProjection(1L, current.plusMonths(1).atDay(1))));
    when(transactionTagRepository.findTagsOfTransactions(eq(USER_ID), anyList()))
        .thenReturn(List.of(new TransactionTagProjection(1L, 11L, "Abonelik")));

    ReportRecurringResponseDto response = reportService.recurring(USER_ID, recurringRequest());

    ArgumentCaptor<LocalDate> startDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
    verify(installmentRepository)
        .findRecurringCandidates(eq(USER_ID), anyList(), startDateCaptor.capture(), any(), eq(InstallmentStatuses.SKIPPED));

    assertEquals(current.minusMonths(5).atDay(1), startDateCaptor.getValue());
    assertEquals(1, response.getItems().size());
    assertEquals(RecurringKinds.INSTALLMENT, response.getItems().get(0).getKind());
    assertEquals("TX:1", response.getItems().get(0).getGroupKey());
    assertEquals(6, response.getItems().get(0).getAmountByMonth().size());
    assertEquals(1, response.getItems().get(0).getTags().size());
  }

  @Test
  @DisplayName("Tekrar edenler - ortalama tekrar sayısına değil geçilen ay sayısına bölünmeli, boş ay 0 dönmeli")
  void recurring_shouldAverageByMonthsSpan_whenOccurrencesAndMonthsDiffer() {
    YearMonth current = YearMonth.now();
    YearMonth threeMonthsAgo = current.minusMonths(3);
    YearMonth twoMonthsAgo = current.minusMonths(2);
    YearMonth lastMonth = current.minusMonths(1);

    // 3 farklı ayda 4 geçiş: bir ay hiç yok, son ayda iki kez; tek taksitli olduğu için isimle gruplanır
    when(installmentRepository.findRecurringCandidates(eq(USER_ID), anyList(), any(), any(), eq(InstallmentStatuses.SKIPPED)))
        .thenReturn(List.of(
            new RecurringInstallmentProjection(1L, "Kahve", 1, BigDecimal.valueOf(100), threeMonthsAgo.atDay(5)),
            new RecurringInstallmentProjection(2L, "Kahve", 1, BigDecimal.valueOf(100), lastMonth.atDay(5)),
            new RecurringInstallmentProjection(3L, "Kahve", 1, BigDecimal.valueOf(100), current.atDay(1)),
            new RecurringInstallmentProjection(4L, "kahve ", 1, BigDecimal.valueOf(200), current.atDay(1))
        ));

    ReportRecurringResponseDto response = reportService.recurring(USER_ID, recurringRequest());

    assertEquals(1, response.getItems().size());
    RecurringItemDto item = response.getItems().get(0);
    assertEquals(RecurringKinds.REPEATED, item.getKind());
    assertEquals(4, item.getOccurrenceCount());
    assertEquals(3, item.getMonthsSpan());
    assertEquals(0, new BigDecimal("500.00").compareTo(item.getTotalAmount()));
    // 500 / 3, 500 / 4 değil
    assertEquals(0, new BigDecimal("166.67").compareTo(item.getAverageAmount()));

    // Geçiş olmayan ay seride 0 ile korunur
    RecurringMonthAmountDto gap = item.getAmountByMonth().stream()
        .filter(month -> month.getYear() == twoMonthsAgo.getYear() && month.getMonth() == twoMonthsAgo.getMonthValue())
        .findFirst()
        .orElseThrow();
    assertEquals(0, BigDecimal.ZERO.compareTo(gap.getAmount()));
    assertEquals(0, new BigDecimal("300.00").compareTo(item.getAmountByMonth().getLast().getAmount()));
  }

  @Test
  @DisplayName("Tekrar edenler - hiç veri yoksa boş liste ve sıfır sabit gider dönmeli")
  void recurring_shouldReturnEmptyResult_whenUserHasNoData() {
    YearMonth current = YearMonth.now();

    ReportRecurringResponseDto response = reportService.recurring(USER_ID, recurringRequest());

    assertTrue(response.getItems().isEmpty());
    assertEquals(0, BigDecimal.ZERO.compareTo(response.getMonthlyFixedCost()));
    assertEquals(0.0, response.getFixedCostShareOfIncome());
    // Pencere bilgisi items boş olsa bile dolu gelmeli
    assertEquals(current.minusMonths(5).getYear(), response.getWindowStart().getYear());
    assertEquals(current.minusMonths(5).getMonthValue(), response.getWindowStart().getMonth());
    assertEquals(current.getYear(), response.getWindowEnd().getYear());
    assertEquals(current.getMonthValue(), response.getWindowEnd().getMonth());
    assertEquals(current.getMonth().name(), response.getWindowEnd().getLabel());
    verify(installmentRepository, never()).findNextDueDates(any(), anyList(), any());
    verify(transactionTagRepository, never()).findTagsOfTransactions(any(), anyList());
  }

  private MonthlyTypeAmountProjection amount(YearMonth period, TransactionTypes type, boolean paid, int amount) {
    return new MonthlyTypeAmountProjection(period.getYear(), period.getMonthValue(), type, paid, BigDecimal.valueOf(amount));
  }

  private ReportSummaryRequestDto summaryRequest(YearMonth period) {
    return new ReportSummaryRequestDto(period.getYear(), period.getMonthValue());
  }

  private ReportTimelineRequestDto timelineRequest(YearMonth period, Integer pastMonths, Integer futureMonths) {
    return new ReportTimelineRequestDto(period.getYear(), period.getMonthValue(), pastMonths, futureMonths);
  }

  private ReportTagBreakdownRequestDto tagBreakdownRequest(YearMonth period) {
    ReportTagBreakdownRequestDto request = new ReportTagBreakdownRequestDto();
    request.setYear(period.getYear());
    request.setMonth(period.getMonthValue());

    return request;
  }

  private ReportTopExpensesRequestDto topExpensesRequest(YearMonth period) {
    ReportTopExpensesRequestDto request = new ReportTopExpensesRequestDto();
    request.setYear(period.getYear());
    request.setMonth(period.getMonthValue());

    return request;
  }

  private ReportRecurringRequestDto recurringRequest() {
    return new ReportRecurringRequestDto();
  }
}
