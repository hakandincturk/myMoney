package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.request.TagSummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.TagSummaryDataDto;
import com.hakandincturk.dtos.dashboard.response.TagSummaryResponseDto;
import com.hakandincturk.dtos.dashboard.response.IncomingInstallmentsDataDto;
import com.hakandincturk.dtos.dashboard.response.IncomingInstallmentsResponseDto;
import com.hakandincturk.dtos.dashboard.response.LastTransactionDataDto;
import com.hakandincturk.dtos.dashboard.response.LastTransactionsResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewIncomeAndExpenseDetailDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.dtos.report.response.PeriodTotalsDto;
import com.hakandincturk.factories.DashboardSummaryFactory;
import com.hakandincturk.factories.ReportPeriodFactory;
import com.hakandincturk.repositories.projections.MonthlyTypeAmountProjection;
import com.hakandincturk.mappers.InstallmentMapper;
import com.hakandincturk.mappers.TransactionMapper;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.repositories.projections.InstallmentTagAmountProjection;
import com.hakandincturk.services.abstracts.DashboardService;
import com.hakandincturk.services.rules.DashboardRules;
import com.hakandincturk.utils.ReportMathUtils;
import com.hakandincturk.utils.TransactionClassifier;

import static java.time.temporal.TemporalAdjusters.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private final DashboardRules dashboardRules;
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final InstallmentRepository installmentRepository;
  private final TransactionMapper transactionMapper;
  private final InstallmentMapper installmentMapper;
  private final ReportPeriodFactory reportPeriodFactory;
  private final DashboardSummaryFactory dashboardSummaryFactory;

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final int TREND_PAST_MONTHS = 6;
  private static final int TREND_FUTURE_MONTHS = 5;

  @Override
  public QuickViewResponseDto quickViewResponse(Long userId) {

    List<AccountTypes> accountTypes = List.of(AccountTypes.BANK, AccountTypes.CASH);
    BigDecimal totalBalance = accountRepository.sumBalanceByUserIdAndTypes(userId, accountTypes);

    LocalDate today = LocalDate.now();
    YearMonth currentPeriod = YearMonth.from(today);
    YearMonth previousPeriod = currentPeriod.minusMonths(1);

    // Computed live from installments on every request, so a deleted or edited transaction is reflected immediately
    Map<YearMonth, PeriodTotalsDto> totalsByDebtDate = this.loadTotalsByDebtDate(userId, previousPeriod, currentPeriod, today);
    PeriodTotalsDto currentByDebtDate = totalsByDebtDate.get(currentPeriod);
    PeriodTotalsDto previousByDebtDate = totalsByDebtDate.get(previousPeriod);
    PeriodTotalsDto currentByPaidDate = this.loadTotalsByPaidDate(userId, currentPeriod, currentPeriod, today).get(currentPeriod);

    QuickViewIncomeAndExpenseDetailDto incomeDetail = dashboardSummaryFactory.buildIncomeDetail(currentByDebtDate, previousByDebtDate, currentByPaidDate);
    QuickViewIncomeAndExpenseDetailDto expenseDetail = dashboardSummaryFactory.buildExpenseDetail(currentByDebtDate, previousByDebtDate, currentByPaidDate);

    BigDecimal totalIncome = incomeDetail.getWaiting().add(incomeDetail.getOccured());
    // Gerçekleşen gider de hesaba katılır; aksi halde oran şişik çıkar ve rapor ekranıyla çelişir
    BigDecimal totalExpense = expenseDetail.getWaiting().add(expenseDetail.getOccured());

    QuickViewResponseDto quickViewResponse = new QuickViewResponseDto();
    quickViewResponse.setTotalBalance(totalBalance);
    quickViewResponse.setIncome(incomeDetail);
    quickViewResponse.setExpense(expenseDetail);
    // Tasarruf oranı rapor modülüyle aynı formülden gelir
    quickViewResponse.setSavingRate(ReportMathUtils.savingRate(totalIncome, totalExpense));

    List<TransactionStatuses> transactionStatuses = List.of(TransactionStatuses.PARTIAL, TransactionStatuses.PENDING);
    List<TransactionTypes> transactionTypes = List.of(TransactionTypes.DEBT, TransactionTypes.PAYMENT);

    Long waitingInstallments = transactionRepository.findWaitingTransactionCount(userId, transactionStatuses, transactionTypes);
    quickViewResponse.setWaitingInstallments(Integer.valueOf(waitingInstallments.toString()));

    return quickViewResponse;
  }

  @Override
  public MonthlyTrendResponseDto monthlyTrend(Long userId) {
    LocalDate today = LocalDate.now();
    YearMonth currentPeriod = YearMonth.from(today);
    // 6 months back to 5 months ahead: the same 12-month window the chart always had
    YearMonth firstPeriod = currentPeriod.minusMonths(TREND_PAST_MONTHS);
    YearMonth lastPeriod = currentPeriod.plusMonths(TREND_FUTURE_MONTHS);

    // One grouped query for the whole window, not one per month
    Map<YearMonth, PeriodTotalsDto> totalsByDebtDate = this.loadTotalsByDebtDate(userId, firstPeriod, lastPeriod, today);

    MonthlyTrendResponseDto responseData = new MonthlyTrendResponseDto();
    responseData.setMonthlyTrendData(dashboardSummaryFactory.buildTrend(totalsByDebtDate, firstPeriod, lastPeriod));

    return responseData;
  }

  /**
   * Month totals by due date (paid and still waiting), from the same query and factory the reports use.
  */
  private Map<YearMonth, PeriodTotalsDto> loadTotalsByDebtDate(Long userId, YearMonth firstPeriod, YearMonth lastPeriod, LocalDate today){
    List<MonthlyTypeAmountProjection> amounts = installmentRepository.sumMonthlyAmountsByTypeAndPaidState(
      userId, firstPeriod.atDay(1), lastPeriod.atEndOfMonth(), InstallmentStatuses.SKIPPED
    );

    return reportPeriodFactory.buildPeriodTotals(firstPeriod, lastPeriod, amounts, List.of(), today);
  }

  /**
   * Month totals by the date the installments were paid; every amount lands in the paid (realized) bucket.
  */
  private Map<YearMonth, PeriodTotalsDto> loadTotalsByPaidDate(Long userId, YearMonth firstPeriod, YearMonth lastPeriod, LocalDate today){
    List<MonthlyTypeAmountProjection> amounts = installmentRepository.sumPaidAmountsByPaidMonthAndType(
      userId, firstPeriod.atDay(1), lastPeriod.atEndOfMonth(), InstallmentStatuses.SKIPPED
    );

    return reportPeriodFactory.buildPeriodTotals(firstPeriod, lastPeriod, amounts, List.of(), today);
  }

  @Override
  public TagSummaryResponseDto tagSummary(Long userId, DashboardTagSummaryTypes type, DashboardTagSummarySumMode sumMode, TagSummaryRequestDto body) {
    LocalDate currentDate = LocalDate.now();
    body.setStartDate(body.getStartDate() == null ? currentDate.withDayOfMonth(1) : body.getStartDate());
    body.setEndDate(body.getEndDate() == null ? currentDate.with(lastDayOfMonth()) : body.getEndDate());

    dashboardRules.tagSummaryDatesControl(body);
    dashboardRules.tagSummaryDatesOnly1MonthOr1Year(body);

    // Gider sınıflandırması aylık özet ve rapor modülüyle aynı kaynaktan gelir
    List<TransactionTypes> transactionTypes = TransactionClassifier.EXPENSE_TYPES;

    // Taksit - etiket satırları tek sorguda çekilir; taksit başına lazy etiket yüklemesi (N+1) yok
    List<InstallmentTagAmountProjection> tagRows = installmentRepository.findInstallmentTagAmounts(
      userId,
      transactionTypes,
      body.getStartDate(),
      body.getEndDate(),
      InstallmentStatuses.SKIPPED
    );

    Map<Long, BigDecimal> amountByInstallment = new LinkedHashMap<>();
    Map<Long, List<String>> tagNamesByInstallment = new LinkedHashMap<>();
    for (InstallmentTagAmountProjection row : tagRows) {
      amountByInstallment.putIfAbsent(row.installmentId(), row.amount());
      List<String> tagNames = tagNamesByInstallment.computeIfAbsent(row.installmentId(), id -> new ArrayList<>());
      if(row.tagName() != null && !tagNames.contains(row.tagName())){
        tagNames.add(row.tagName());
      }
    }

    BigDecimal totalAmount = ZERO;
    Map<String, BigDecimal> tagSummary = new HashMap<>();
    for (Map.Entry<Long, BigDecimal> installmentEntry : amountByInstallment.entrySet()) {
      BigDecimal amount = installmentEntry.getValue();
      totalAmount = totalAmount.add(amount);

      List<String> installmentTags = tagNamesByInstallment.getOrDefault(installmentEntry.getKey(), List.of());
      if(installmentTags.isEmpty()){
        tagSummary.merge("-", amount, BigDecimal::add);
        continue;
      }

      if(sumMode == DashboardTagSummarySumMode.DISTRIBUTED){
        int tagSize = installmentTags.size();
        amount = amount.divide(BigDecimal.valueOf(tagSize), 2, RoundingMode.HALF_UP);
      }

      for (String tagName : installmentTags) {
        tagSummary.merge(tagName, amount, BigDecimal::add);
      }
    }

    // 0'a bolunme hatasi
    final BigDecimal total = totalAmount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : totalAmount;

    TagSummaryResponseDto response = new TagSummaryResponseDto();
    List<TagSummaryDataDto> summaries = new ArrayList<>();
    tagSummary.forEach((tagName, amount) -> {
      TagSummaryDataDto summaryData = new TagSummaryDataDto();
      summaryData.setName(tagName);
      summaryData.setAmount(amount);

      Double percentage = amount.divide(total, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
      summaryData.setPercentage(percentage);

      summaries.add(summaryData);
    });
    response.setTagSummaryDatas(summaries);

    return response;
  }

  @Override
  public LastTransactionsResponseDto lastTransactions(Long userId) {
    List<Transaction> dbTransactions = transactionRepository.findTop10ByUserIdAndIsRemovedFalseOrderByIdDesc(userId);
    List<LastTransactionDataDto> mappedTransactions = dbTransactions.stream().map(transactionMapper::toLastTransactionDataDto).toList();

    LastTransactionsResponseDto transactions = new LastTransactionsResponseDto();
    transactions.setLastTransactionData(mappedTransactions);

    return transactions;
  }

  @Override
  public IncomingInstallmentsResponseDto incomingInstallments(Long userId) {
    LocalDate currenDate = LocalDate.now();
    LocalDate startDate = currenDate.minusDays(1);
    LocalDate endDate = currenDate.plusDays(30);

    List<Installment> dbInstallments = installmentRepository.findTop10ByTransaction_UserIdAndDebtDateBetweenAndIsPaidFalseAndIsRemovedFalseOrderByDebtDate(userId, startDate, endDate);
    List<IncomingInstallmentsDataDto> mappedInstallments = dbInstallments.stream().map(installmentMapper::toIncomingInstallmentsData).toList();

    IncomingInstallmentsResponseDto installments = new IncomingInstallmentsResponseDto();
    installments.setIncomingInstallmentsDatas(mappedInstallments);

    return installments;
  }
}
