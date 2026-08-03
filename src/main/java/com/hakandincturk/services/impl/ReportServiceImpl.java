package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.PeriodKind;
import com.hakandincturk.core.enums.ReportFlowTypes;
import com.hakandincturk.dtos.report.request.ReportRecurringRequestDto;
import com.hakandincturk.dtos.report.request.ReportSummaryRequestDto;
import com.hakandincturk.dtos.report.request.ReportTagBreakdownRequestDto;
import com.hakandincturk.dtos.report.request.ReportTimelineRequestDto;
import com.hakandincturk.dtos.report.request.ReportTopExpensesRequestDto;
import com.hakandincturk.dtos.report.response.BusiestDayDto;
import com.hakandincturk.dtos.report.response.PeriodDeltaDto;
import com.hakandincturk.dtos.report.response.PeriodTotalsDto;
import com.hakandincturk.dtos.report.response.ReportRecurringResponseDto;
import com.hakandincturk.dtos.report.response.ReportSummaryResponseDto;
import com.hakandincturk.dtos.report.response.ReportTagBreakdownResponseDto;
import com.hakandincturk.dtos.report.response.ReportTagRefDto;
import com.hakandincturk.dtos.report.response.ReportTimelineResponseDto;
import com.hakandincturk.dtos.report.response.ReportTopExpensesResponseDto;
import com.hakandincturk.dtos.report.response.TimelinePointDto;
import com.hakandincturk.dtos.report.response.TopExpenseItemDto;
import com.hakandincturk.factories.ReportPeriodFactory;
import com.hakandincturk.factories.ReportRecurringFactory;
import com.hakandincturk.factories.ReportTagFactory;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.repositories.projections.DailyAmountProjection;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;
import com.hakandincturk.repositories.projections.MonthlyEntityCountProjection;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;
import com.hakandincturk.repositories.projections.RecurringInstallmentProjection;
import com.hakandincturk.repositories.projections.TopInstallmentProjection;
import com.hakandincturk.repositories.projections.TransactionNextDueProjection;
import com.hakandincturk.repositories.projections.TransactionTagProjection;
import com.hakandincturk.services.abstracts.ReportService;
import com.hakandincturk.services.rules.ReportRules;
import com.hakandincturk.utils.ReportMathUtils;
import com.hakandincturk.utils.ReportPeriodUtils;
import com.hakandincturk.utils.TransactionClassifier;

import lombok.RequiredArgsConstructor;

/**
 * Rapor ekranının veri kaynağı.
 * Bir ayın gelir/gideri, o aya düşen taksit satırlarından hesaplanır (transaction'ın toplam
 * tutarından değil); böylece taksitli işlemler doğru aya dağılır ve gelecek aylar projekte edilir.
 * Ay bazlı toplamlar tek sorguda GROUP BY ile çekilir, döngü içinde sorgu açılmaz.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

  private final ReportRules reportRules;
  private final ReportPeriodFactory reportPeriodFactory;
  private final ReportTagFactory reportTagFactory;
  private final ReportRecurringFactory reportRecurringFactory;
  private final InstallmentRepository installmentRepository;
  private final TransactionTagRepository transactionTagRepository;

  // Ödenmeyecek taksitler hiçbir toplama girmediği için tüm sorgulara sabit olarak geçilir
  private static final InstallmentStatuses SKIPPED_STATUS = InstallmentStatuses.SKIPPED;

  @Override
  public ReportSummaryResponseDto summary(Long userId, ReportSummaryRequestDto request) {
    reportRules.checkPeriodIsValid(request.getYear(), request.getMonth());

    LocalDate today = LocalDate.now();
    YearMonth currentPeriod = YearMonth.of(request.getYear(), request.getMonth());
    YearMonth previousPeriod = currentPeriod.minusMonths(1);
    YearMonth nextPeriod = currentPeriod.plusMonths(1);

    // Üç ay tek sorgu çiftiyle çekilir; veri olmayan aylar sıfırlı kayıt olarak döner
    Map<YearMonth, PeriodTotalsDto> periodTotals = this.loadPeriodTotals(userId, previousPeriod, nextPeriod, today);
    PeriodTotalsDto currentTotals = periodTotals.get(currentPeriod);

    List<InstallmentTagAmountProjection> tagRows = this.loadTagRows(userId, ReportFlowTypes.EXPENSE, currentPeriod);

    int daysInMonth = currentPeriod.lengthOfMonth();
    int daysElapsed = ReportPeriodUtils.divisorDayCount(currentPeriod, today);
    BigDecimal averageDailyExpense = ReportMathUtils.divide(currentTotals.getExpense(), daysElapsed);

    ReportSummaryResponseDto response = new ReportSummaryResponseDto();
    response.setPrevious(periodTotals.get(previousPeriod));
    response.setCurrent(currentTotals);
    response.setNext(periodTotals.get(nextPeriod));
    response.setDeltaVsPrevious(this.buildDelta(periodTotals.get(previousPeriod), currentTotals));
    response.setDeltaVsNext(this.buildDelta(currentTotals, periodTotals.get(nextPeriod)));
    response.setSavingRate(ReportMathUtils.savingRate(currentTotals.getIncome(), currentTotals.getExpense()));
    response.setAverageDailyExpense(averageDailyExpense);
    response.setDaysInMonth(daysInMonth);
    response.setDaysElapsed(daysElapsed);
    response.setProjectedMonthEndExpense(this.projectMonthEndExpense(currentTotals, averageDailyExpense, daysInMonth));
    response.setBusiestDay(this.findBusiestDay(userId, currentPeriod));
    response.setTopTag(reportTagFactory.buildTopTag(tagRows));

    return response;
  }

  @Override
  public ReportTimelineResponseDto timeline(Long userId, ReportTimelineRequestDto request) {
    reportRules.checkPeriodIsValid(request.getYear(), request.getMonth());

    LocalDate today = LocalDate.now();
    YearMonth centerPeriod = YearMonth.of(request.getYear(), request.getMonth());

    int pastMonths = ReportPeriodUtils.normalize(request.getPastMonths(), ReportTimelineRequestDto.DEFAULT_PAST_MONTHS, 0, ReportTimelineRequestDto.MAX_MONTHS);
    int futureMonths = ReportPeriodUtils.normalize(request.getFutureMonths(), ReportTimelineRequestDto.DEFAULT_FUTURE_MONTHS, 0, ReportTimelineRequestDto.MAX_MONTHS);

    YearMonth startPeriod = centerPeriod.minusMonths(pastMonths);
    YearMonth endPeriod = centerPeriod.plusMonths(futureMonths);

    Map<YearMonth, PeriodTotalsDto> periodTotals = this.loadPeriodTotals(userId, startPeriod, endPeriod, today);
    List<YearMonth> months = ReportPeriodUtils.monthsBetween(startPeriod, endPeriod);
    Map<YearMonth, BigDecimal> remainingDebtByMonth = this.calculateRemainingDebt(userId, months);
    Map<YearMonth, BigDecimal> projectedRemainingDebtByMonth = this.calculateProjectedRemainingDebt(userId, months);

    List<TimelinePointDto> points = new ArrayList<>();
    for (YearMonth month : months) {
      BigDecimal remainingDebt = ReportMathUtils.money(remainingDebtByMonth.get(month));
      BigDecimal projectedRemainingDebt = ReportMathUtils.money(projectedRemainingDebtByMonth.get(month));
      points.add(new TimelinePointDto(periodTotals.get(month), remainingDebt, projectedRemainingDebt));
    }

    ReportTimelineResponseDto response = new ReportTimelineResponseDto();
    response.setPoints(points);

    return response;
  }

  @Override
  public ReportTagBreakdownResponseDto tagBreakdown(Long userId, ReportTagBreakdownRequestDto request) {
    reportRules.checkPeriodIsValid(request.getYear(), request.getMonth());

    YearMonth currentPeriod = YearMonth.of(request.getYear(), request.getMonth());
    YearMonth previousPeriod = currentPeriod.minusMonths(1);

    ReportFlowTypes flowType = this.resolveFlowType(request.getType());
    DashboardTagSummarySumMode sumMode = this.resolveSumMode(request.getSumMode());
    int limit = ReportPeriodUtils.normalize(request.getLimit(), ReportTagBreakdownRequestDto.DEFAULT_LIMIT, 0, ReportTagBreakdownRequestDto.MAX_LIMIT);

    List<InstallmentTagAmountProjection> currentRows = this.loadTagRows(userId, flowType, currentPeriod);
    List<InstallmentTagAmountProjection> previousRows = this.loadTagRows(userId, flowType, previousPeriod);

    return reportTagFactory.buildBreakdown(currentRows, previousRows, sumMode, limit);
  }

  @Override
  public ReportTopExpensesResponseDto topExpenses(Long userId, ReportTopExpensesRequestDto request) {
    reportRules.checkPeriodIsValid(request.getYear(), request.getMonth());

    LocalDate today = LocalDate.now();
    YearMonth period = YearMonth.of(request.getYear(), request.getMonth());
    ReportFlowTypes flowType = this.resolveFlowType(request.getType());
    int limit = ReportPeriodUtils.normalize(request.getLimit(), ReportTopExpensesRequestDto.DEFAULT_LIMIT, 0, ReportTopExpensesRequestDto.MAX_LIMIT);

    PeriodTotalsDto periodTotals = this.loadPeriodTotals(userId, period, period, today).get(period);
    BigDecimal periodTotal = flowType == ReportFlowTypes.INCOME ? periodTotals.getIncome() : periodTotals.getExpense();

    ReportTopExpensesResponseDto response = new ReportTopExpensesResponseDto();
    response.setItems(this.loadTopExpenseItems(userId, flowType, period, limit));
    response.setPeriodTotal(periodTotal);

    return response;
  }

  @Override
  public ReportRecurringResponseDto recurring(Long userId, ReportRecurringRequestDto request) {
    LocalDate today = LocalDate.now();
    YearMonth currentPeriod = YearMonth.from(today);

    int lookbackMonths = ReportPeriodUtils.normalize(request.getLookbackMonths(), ReportRecurringRequestDto.DEFAULT_LOOKBACK_MONTHS, 1, ReportRecurringRequestDto.MAX_LOOKBACK_MONTHS);
    int minOccurrence = ReportPeriodUtils.normalize(request.getMinOccurrence(), ReportRecurringRequestDto.DEFAULT_MIN_OCCURRENCE, 1, ReportRecurringRequestDto.MAX_MIN_OCCURRENCE);
    int limit = ReportPeriodUtils.normalize(request.getLimit(), ReportRecurringRequestDto.DEFAULT_LIMIT, 0, ReportRecurringRequestDto.MAX_LIMIT);

    // Pencere geçmişe doğru bakar ve içinde bulunulan ayı da kapsar
    YearMonth startPeriod = currentPeriod.minusMonths(lookbackMonths - 1L);
    List<YearMonth> windowMonths = ReportPeriodUtils.monthsBetween(startPeriod, currentPeriod);

    List<RecurringInstallmentProjection> rows = installmentRepository.findRecurringCandidates(
      userId,
      TransactionClassifier.EXPENSE_TYPES,
      startPeriod.atDay(1),
      currentPeriod.atEndOfMonth(),
      SKIPPED_STATUS
    );

    List<Long> transactionIds = rows.stream().map(RecurringInstallmentProjection::transactionId).distinct().toList();
    Map<Long, List<ReportTagRefDto>> tagsByTransaction = this.loadTagsOfTransactions(userId, transactionIds);
    Map<Long, LocalDate> nextDueByTransaction = this.loadNextDueDates(userId, transactionIds);

    BigDecimal currentMonthIncome = this.loadPeriodTotals(userId, currentPeriod, currentPeriod, today).get(currentPeriod).getIncome();

    return reportRecurringFactory.build(rows, windowMonths, tagsByTransaction, nextDueByTransaction, minOccurrence, limit, today, currentMonthIncome);
  }

  /**
   * Aralıktaki tüm ayların toplamlarını iki sorguyla yükler (tutarlar + adetler).
  */
  private Map<YearMonth, PeriodTotalsDto> loadPeriodTotals(Long userId, YearMonth startPeriod, YearMonth endPeriod, LocalDate today){
    LocalDate startDate = startPeriod.atDay(1);
    LocalDate endDate = endPeriod.atEndOfMonth();

    List<MonthlyTypeAmountProjection> amounts = installmentRepository.sumMonthlyAmountsByTypeAndPaidState(userId, startDate, endDate, SKIPPED_STATUS);
    List<MonthlyEntityCountProjection> counts = installmentRepository.countMonthlyEntities(userId, startDate, endDate, SKIPPED_STATUS);

    return reportPeriodFactory.buildPeriodTotals(startPeriod, endPeriod, amounts, counts, today);
  }

  /**
   * İki dönem arasındaki geçişi zaman yönünde ileri okur: from -> to.
   * Oranın paydası her zaman önce gelen dönemdir; bu yüzden deltaVsPrevious'ta payda previous,
   * deltaVsNext'te payda current olur.
   * @param from Geçişin başladığı dönem (payda)
   * @param to Geçişin bittiği dönem
  */
  private PeriodDeltaDto buildDelta(PeriodTotalsDto from, PeriodTotalsDto to){
    PeriodDeltaDto delta = new PeriodDeltaDto();
    delta.setIncomeChangeRate(ReportMathUtils.changeRate(to.getIncome(), from.getIncome()));
    delta.setExpenseChangeRate(ReportMathUtils.changeRate(to.getExpense(), from.getExpense()));
    delta.setNetChangeAmount(ReportMathUtils.money(to.getNet().subtract(from.getNet())));

    return delta;
  }

  /**
   * Ayın toplam giderinin en yüksek olduğu tek gün; gider yoksa null döner.
  */
  private BusiestDayDto findBusiestDay(Long userId, YearMonth period){
    List<DailyAmountProjection> dailyAmounts = installmentRepository.sumDailyAmounts(
      userId,
      TransactionClassifier.EXPENSE_TYPES,
      period.atDay(1),
      period.atEndOfMonth(),
      SKIPPED_STATUS
    );

    return dailyAmounts.stream()
      .filter(dailyAmount -> ReportMathUtils.zeroIfNull(dailyAmount.amount()).signum() != 0)
      .max(
        Comparator.comparing(DailyAmountProjection::amount)
          .thenComparing(DailyAmountProjection::date, Comparator.reverseOrder())
      )
      .map(dailyAmount -> new BusiestDayDto(dailyAmount.date(), ReportMathUtils.money(dailyAmount.amount())))
      .orElse(null);
  }

  /**
   * Ayın sonu itibarıyla ödenmesi gereken kalan borcu her ay için hesaplar.
   * Halen ödenmemiş toplam borcun üzerine, o aydan sonra ödenmiş taksitler geri eklenir.
  */
  private Map<YearMonth, BigDecimal> calculateRemainingDebt(Long userId, List<YearMonth> months){
    BigDecimal unpaidTotal = ReportMathUtils.zeroIfNull(
      installmentRepository.sumUnpaidAmount(userId, TransactionClassifier.EXPENSE_TYPES, SKIPPED_STATUS)
    );

    YearMonth firstMonth = months.getFirst();
    YearMonth lastMonth = months.getLast();

    Map<YearMonth, BigDecimal> paidByMonth = installmentRepository
      .sumPaidAmountsByPaidMonth(userId, TransactionClassifier.EXPENSE_TYPES, firstMonth.atDay(1), SKIPPED_STATUS)
      .stream()
      .collect(Collectors.toMap(
        projection -> YearMonth.of(projection.year(), projection.month()),
        projection -> ReportMathUtils.zeroIfNull(projection.amount()),
        BigDecimal::add
      ));

    // Pencerenin sonrasında ödenmiş taksitler, penceredeki her ayın kalan borcuna dahildir
    BigDecimal runningRemainingDebt = unpaidTotal.add(
      paidByMonth.entrySet().stream()
        .filter(entry -> entry.getKey().isAfter(lastMonth))
        .map(Map.Entry::getValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
    );

    Map<YearMonth, BigDecimal> remainingDebtByMonth = new HashMap<>();
    for (int index = months.size() - 1; index >= 0; index--) {
      YearMonth month = months.get(index);
      remainingDebtByMonth.put(month, runningRemainingDebt);
      // Bir önceki aya geçerken, bu ayda ödenen tutar da "henüz ödenmemiş" sayılır
      runningRemainingDebt = runningRemainingDebt.add(paidByMonth.getOrDefault(month, BigDecimal.ZERO));
    }

    return remainingDebtByMonth;
  }

  /**
   * Ayın sonundan sonraki vadeye sahip ve halen ödenmemiş taksitlerin toplamı.
   * "Plana uyulursa borç nasıl erir" eğrisini verir; bu yüzden vadesi geçmiş ödenmemiş taksitler
   * kendi aylarından sonraki noktalarda düşer. calculateRemainingDebt ise geçmişe bakar ve
   * o ayın sonunda fiilen taşınan borcu verir - iki metrik bilerek farklıdır.
  */
  private Map<YearMonth, BigDecimal> calculateProjectedRemainingDebt(Long userId, List<YearMonth> months){
    YearMonth firstMonth = months.getFirst();
    YearMonth lastMonth = months.getLast();

    Map<YearMonth, BigDecimal> unpaidByDebtMonth = installmentRepository
      .sumUnpaidAmountsByDebtMonth(userId, TransactionClassifier.EXPENSE_TYPES, firstMonth.atDay(1), SKIPPED_STATUS)
      .stream()
      .collect(Collectors.toMap(
        projection -> YearMonth.of(projection.year(), projection.month()),
        projection -> ReportMathUtils.zeroIfNull(projection.amount()),
        BigDecimal::add
      ));

    // Pencerenin sonrasına düşen ödenmemiş taksitler penceredeki her ayın projeksiyonuna dahildir
    BigDecimal runningProjectedDebt = unpaidByDebtMonth.entrySet().stream()
      .filter(entry -> entry.getKey().isAfter(lastMonth))
      .map(Map.Entry::getValue)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    Map<YearMonth, BigDecimal> projectedDebtByMonth = new HashMap<>();
    for (int index = months.size() - 1; index >= 0; index--) {
      YearMonth month = months.get(index);
      projectedDebtByMonth.put(month, runningProjectedDebt);
      // Bir önceki aya geçerken, bu ayın vadesi de "gelecekte ödenecek" hale gelir
      runningProjectedDebt = runningProjectedDebt.add(unpaidByDebtMonth.getOrDefault(month, BigDecimal.ZERO));
    }

    return projectedDebtByMonth;
  }

  /**
   * İçinde bulunulan ayda ay sonu gideri için naif doğrusal tahmin; diğer aylarda ayın kendi gideri.
  */
  private BigDecimal projectMonthEndExpense(PeriodTotalsDto currentTotals, BigDecimal averageDailyExpense, int daysInMonth){
    if(currentTotals.getKind() != PeriodKind.PARTIAL){
      return ReportMathUtils.money(currentTotals.getExpense());
    }

    return ReportMathUtils.money(averageDailyExpense.multiply(BigDecimal.valueOf(daysInMonth)));
  }

  private List<InstallmentTagAmountProjection> loadTagRows(Long userId, ReportFlowTypes flowType, YearMonth period){
    return installmentRepository.findInstallmentTagAmounts(
      userId,
      TransactionClassifier.typesOf(flowType),
      period.atDay(1),
      period.atEndOfMonth(),
      SKIPPED_STATUS
    );
  }

  private List<TopExpenseItemDto> loadTopExpenseItems(Long userId, ReportFlowTypes flowType, YearMonth period, int limit){
    if(limit == 0){
      return List.of();
    }

    List<TopInstallmentProjection> rows = installmentRepository.findTopInstallments(
      userId,
      TransactionClassifier.typesOf(flowType),
      period.atDay(1),
      period.atEndOfMonth(),
      SKIPPED_STATUS,
      PageRequest.of(0, limit)
    );

    List<Long> transactionIds = rows.stream().map(TopInstallmentProjection::transactionId).distinct().toList();
    Map<Long, List<ReportTagRefDto>> tagsByTransaction = this.loadTagsOfTransactions(userId, transactionIds);

    return rows.stream().map(row -> this.toTopExpenseItem(row, tagsByTransaction)).toList();
  }

  private TopExpenseItemDto toTopExpenseItem(TopInstallmentProjection row, Map<Long, List<ReportTagRefDto>> tagsByTransaction){
    TopExpenseItemDto item = new TopExpenseItemDto();
    item.setTransactionId(row.transactionId());
    item.setInstallmentId(row.installmentId());
    item.setName(row.transactionName());
    // Taksit özelinde açıklama girilmişse o gösterilir, yoksa işlemin açıklamasına düşülür
    item.setDescription(row.installmentDescription() != null ? row.installmentDescription() : row.transactionDescription());
    item.setAmount(ReportMathUtils.money(row.amount()));
    item.setDate(row.debtDate());
    item.setAccountName(row.accountName());
    item.setContactName(row.contactName());
    item.setTags(tagsByTransaction.getOrDefault(row.transactionId(), List.of()));
    item.setInstallmentNumber(row.installmentNumber());
    item.setTotalInstallment(row.totalInstallment());
    item.setPaid(Boolean.TRUE.equals(row.paid()));

    return item;
  }

  /**
   * Birden fazla işlemin etiketlerini tek sorguda yükler, böylece kalem başına sorgu açılmaz.
  */
  private Map<Long, List<ReportTagRefDto>> loadTagsOfTransactions(Long userId, List<Long> transactionIds){
    if(transactionIds.isEmpty()){
      return Map.of();
    }

    return transactionTagRepository.findTagsOfTransactions(userId, transactionIds).stream()
      .collect(Collectors.groupingBy(
        TransactionTagProjection::transactionId,
        LinkedHashMap::new,
        Collectors.mapping(projection -> new ReportTagRefDto(projection.tagId(), projection.tagName()), Collectors.toList())
      ));
  }

  private Map<Long, LocalDate> loadNextDueDates(Long userId, List<Long> transactionIds){
    if(transactionIds.isEmpty()){
      return Map.of();
    }

    return installmentRepository.findNextDueDates(userId, transactionIds, SKIPPED_STATUS).stream()
      .filter(projection -> projection.nextDueDate() != null)
      .collect(Collectors.toMap(TransactionNextDueProjection::transactionId, TransactionNextDueProjection::nextDueDate));
  }

  private ReportFlowTypes resolveFlowType(ReportFlowTypes flowType){
    return flowType == null ? ReportFlowTypes.EXPENSE : flowType;
  }

  private DashboardTagSummarySumMode resolveSumMode(DashboardTagSummarySumMode sumMode){
    return sumMode == null ? DashboardTagSummarySumMode.DISTRIBUTED : sumMode;
  }

}
