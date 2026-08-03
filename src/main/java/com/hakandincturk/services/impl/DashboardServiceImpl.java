package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.MonthlySummeryTypes;
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
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendDataDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewIncomeAndExpenseDetailDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.mappers.InstallmentMapper;
import com.hakandincturk.mappers.TransactionMapper;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
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
  private final MonthlySummaryRepository monthlySummaryRepository;
  private final TransactionMapper transactionMapper;
  private final InstallmentMapper installmentMapper;

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  @Override
  public QuickViewResponseDto quickViewResponse(Long userId) {

    List<AccountTypes> accountTypes = List.of(AccountTypes.BANK, AccountTypes.CASH);
    BigDecimal totalBalance = accountRepository.sumBalanceByUserIdAndTypes(userId, accountTypes);

    QuickViewIncomeAndExpenseDetailDto incomeDetail = new QuickViewIncomeAndExpenseDetailDto();
    QuickViewIncomeAndExpenseDetailDto expenseDetail = new QuickViewIncomeAndExpenseDetailDto();

    LocalDateTime currentDate = LocalDateTime.now();
    LocalDateTime prevMonth = currentDate.minusMonths(1);

    // debtDate'e gore monthly summary
    Optional<MonthlySummary> dbMonthlyTransactionSummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(userId, currentDate.getYear(), currentDate.getMonthValue(), MonthlySummeryTypes.TRANSACTION);

    // paymentDate'e gore monthly summary
    Optional<MonthlySummary> dbMonthlyPaymentSummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(userId, currentDate.getYear(), currentDate.getMonthValue(), MonthlySummeryTypes.PAYMENT);

    // onceki ay paymentDate'e gore monthly summary
    Optional<MonthlySummary> dbPrevMonthlPaymentSummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(userId, prevMonth.getYear(), prevMonth.getMonthValue(), MonthlySummeryTypes.PAYMENT);

    incomeDetail.setOccured(ZERO);
    incomeDetail.setWaiting(ZERO);
    incomeDetail.setLastMonthChangeRate(Double.valueOf(0));

    expenseDetail.setOccured(ZERO);
    expenseDetail.setWaiting(ZERO);
    expenseDetail.setLastMonthChangeRate(Double.valueOf(0));

    if(!dbMonthlyTransactionSummary.isEmpty()) {
      MonthlySummary monthlTransactionSummary = dbMonthlyTransactionSummary.get();
      incomeDetail.setWaiting(monthlTransactionSummary.getTotalWaitingIncome());
      expenseDetail.setWaiting(monthlTransactionSummary.getTotalWaitingExpense());

      if(!dbMonthlyPaymentSummary.isEmpty()){
      MonthlySummary monthlPaymentSummary = dbMonthlyPaymentSummary.get();
        incomeDetail.setOccured(monthlPaymentSummary.getTotalIncome());
        expenseDetail.setOccured(monthlPaymentSummary.getTotalExpense());
      }

      if(!dbPrevMonthlPaymentSummary.isEmpty()) {
        // bu ay ve bir onceki ay toplam gelir
        MonthlySummary prevMonthlyPaymentSummary = dbPrevMonthlPaymentSummary.get();
        BigDecimal totalPrevIncome = prevMonthlyPaymentSummary.getTotalIncome();
        BigDecimal totalIncome = monthlTransactionSummary.getTotalWaitingIncome().add(monthlTransactionSummary.getTotalIncome());

        // bu ay ve bir onceki ay toplam gider
        BigDecimal totalPrevMonthExpense = prevMonthlyPaymentSummary.getTotalExpense();
        BigDecimal totalExpense = monthlTransactionSummary.getTotalExpense().add(monthlTransactionSummary.getTotalWaitingExpense());

        // ((gecen ay gelir - bu ay gelir)/gecen ay gelir)*100
        BigDecimal incomeDiffrence = totalIncome.subtract(totalPrevIncome);
        BigDecimal incomeChangeRate = incomeDiffrence.signum() == 0 || totalPrevIncome.signum() == 0 ? ZERO : incomeDiffrence.divide(totalPrevIncome, 2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));;
        incomeDetail.setLastMonthChangeRate(incomeChangeRate.doubleValue());

        // ((gecen ay gider - bu ay gider)/gecen ay gelir)*100
        BigDecimal expenseDiffrence = totalExpense.subtract(totalPrevMonthExpense);
        BigDecimal expenseChangeRate = expenseDiffrence.signum() == 0 || totalPrevMonthExpense.signum() == 0 ? ZERO : expenseDiffrence.divide(totalPrevMonthExpense, 2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));
        expenseDetail.setLastMonthChangeRate(Double.valueOf(expenseChangeRate.doubleValue()));

        // expenseDetail.setOccured(prevMonthlyPaymentSummary.getTotalExpense());
      }
    }

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
    LocalDate currentDate = LocalDate.now().withDayOfMonth(1);
    LocalDate firstDate = currentDate.minusMonths(6);
    LocalDate endDate = currentDate.plusMonths(6);

    List<MonthlySummary> oldSummaries = monthlySummaryRepository.findByUser_IdAndSummaryDateBetweenAndTypeAndIsRemovedFalse(userId, firstDate, currentDate.minusMonths(1), MonthlySummeryTypes.TRANSACTION);
    List<MonthlySummary> futureSummaries = monthlySummaryRepository.findByUser_IdAndSummaryDateBetweenAndTypeAndIsRemovedFalse(userId, currentDate, endDate, MonthlySummeryTypes.TRANSACTION);
    List<MonthlySummary> allSummaries = Stream.concat(oldSummaries.stream(), futureSummaries.stream()).toList();

    Map<LocalDate, MonthlySummary> summaryMap = allSummaries.stream()
      .collect(Collectors.toMap(MonthlySummary::getSummaryDate, Function.identity()));

    List<MonthlyTrendDataDto> monthlyTrendDatas = new ArrayList<>();
    LocalDate dateCursor = firstDate;
    while(dateCursor.isBefore(endDate)){
      MonthlySummary monthlySummary = summaryMap.get(dateCursor);
      MonthlyTrendDataDto monthlyTrendData = new MonthlyTrendDataDto();

      if(monthlySummary == null){
        monthlyTrendData.setExpense(ZERO);
        monthlyTrendData.setIncome(ZERO);
        monthlyTrendData.setTitle(dateCursor.getMonth().toString());
      }
      else{
        BigDecimal income = monthlySummary.getTotalIncome().add(monthlySummary.getTotalWaitingIncome());
        BigDecimal expense = monthlySummary.getTotalExpense().add(monthlySummary.getTotalWaitingExpense());

        monthlyTrendData.setIncome(income);
        monthlyTrendData.setExpense(expense);
        monthlyTrendData.setTitle(monthlySummary.getSummaryDate().getMonth().toString());
      }

      monthlyTrendDatas.add(monthlyTrendData);
      dateCursor = dateCursor.plusMonths(1);
    }
    MonthlyTrendResponseDto responseData = new MonthlyTrendResponseDto();
    responseData.setMonthlyTrendData(monthlyTrendDatas);

    return responseData;
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
