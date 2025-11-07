package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.MonthlySummeryTypes;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrend;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendData;
import com.hakandincturk.dtos.dashboard.response.QuickViewIncomeAndExpenseDetailDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final MonthlySummaryRepository monthlySummaryRepository;

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
      incomeDetail.setPlanning(monthlTransactionSummary.getTotalIncome());
      incomeDetail.setWaiting(monthlTransactionSummary.getTotalWaitingIncome());
      
      expenseDetail.setPlanning(monthlTransactionSummary.getTotalExpense());
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
    BigDecimal totalExpense = expenseDetail.getWaiting();

    BigDecimal savingRate = totalIncome.compareTo(BigDecimal.ZERO) == 0 ? 
      ZERO :
        totalIncome.subtract(totalExpense)
        .divide(totalIncome, 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));

    QuickViewResponseDto quickViewResponse = new QuickViewResponseDto();
    quickViewResponse.setTotalBalance(totalBalance);
    quickViewResponse.setIncome(incomeDetail);
    quickViewResponse.setExpense(expenseDetail);
    quickViewResponse.setSavingRate(savingRate.doubleValue());

    List<TransactionStatuses> transactionStatuses = List.of(TransactionStatuses.PARTIAL, TransactionStatuses.PENDING);
    List<TransactionTypes> transactionTypes = List.of(TransactionTypes.DEBT, TransactionTypes.PAYMENT);

    Long waitingInstallments = transactionRepository.findWaitingTransactions(userId, transactionStatuses, transactionTypes);
    quickViewResponse.setWaitingInstallments(Integer.valueOf(waitingInstallments.toString()));

    return quickViewResponse;
  }

  @Override
  public MonthlyTrend monthlyTrend(Long userId) {
    LocalDate currentDate = LocalDate.now().withDayOfMonth(1);
    LocalDate firstDate = currentDate.minusMonths(6);
    LocalDate endDate = currentDate.plusMonths(6);

    List<MonthlySummary> oldSummaries = monthlySummaryRepository.findByUser_IdAndSummaryDateBetweenAndTypeAndIsRemovedFalse(userId, firstDate, currentDate.minusMonths(1), MonthlySummeryTypes.PAYMENT);
    List<MonthlySummary> futureSummaries = monthlySummaryRepository.findByUser_IdAndSummaryDateBetweenAndTypeAndIsRemovedFalse(userId, currentDate, endDate, MonthlySummeryTypes.TRANSACTION);
    List<MonthlySummary> allSummaries = Stream.concat(oldSummaries.stream(), futureSummaries.stream()).toList();

    Map<LocalDate, MonthlySummary> summaryMap = allSummaries.stream()
      .collect(Collectors.toMap(MonthlySummary::getSummaryDate, Function.identity()));

    List<MonthlyTrendData> monthlyTrendDatas = new ArrayList<>();
    LocalDate dateCursor = firstDate;
    while(dateCursor.isBefore(endDate)){
      MonthlySummary monthlySummary = summaryMap.get(dateCursor);
      MonthlyTrendData monthlyTrendData = new MonthlyTrendData();

      if(monthlySummary == null){
        monthlyTrendData.setExpense(ZERO);
        monthlyTrendData.setIncome(ZERO);
        monthlyTrendData.setTitle(dateCursor.getMonth().toString());
      }
      else{
        BigDecimal income = dateCursor.isBefore(currentDate) || dateCursor.isEqual(currentDate) ? monthlySummary.getTotalIncome() : monthlySummary.getTotalWaitingIncome();
        BigDecimal expense = dateCursor.isBefore(currentDate) ? monthlySummary.getTotalExpense() : monthlySummary.getTotalWaitingExpense();

        monthlyTrendData.setIncome(income);
        monthlyTrendData.setExpense(expense);
        monthlyTrendData.setTitle(monthlySummary.getSummaryDate().getMonth().toString());
      }

      monthlyTrendDatas.add(monthlyTrendData);
      dateCursor = dateCursor.plusMonths(1);
    }
    MonthlyTrend responseData = new MonthlyTrend();
    responseData.setMonthlyTrendData(monthlyTrendDatas);
    System.out.println(1);

    return responseData;
  }
  

  
}
