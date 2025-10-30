package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.MonthlySummeryTypes;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
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

    incomeDetail.setOccured(BigDecimal.valueOf(0));
    incomeDetail.setWaiting(BigDecimal.valueOf(0));
    incomeDetail.setLastMonthChangeRate(Double.valueOf(0));

    expenseDetail.setOccured(BigDecimal.valueOf(0));
    expenseDetail.setWaiting(BigDecimal.valueOf(0));
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
        BigDecimal incomeChangeRate = incomeDiffrence.signum() == 0 || totalPrevIncome.signum() == 0 ? BigDecimal.valueOf(0) : incomeDiffrence.divide(totalPrevIncome, 2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));;
        incomeDetail.setLastMonthChangeRate(incomeChangeRate.doubleValue());
        
        // ((gecen ay gider - bu ay gider)/gecen ay gelir)*100
        BigDecimal expenseDiffrence = totalExpense.subtract(totalPrevMonthExpense);
        BigDecimal expenseChangeRate = expenseDiffrence.signum() == 0 || totalPrevMonthExpense.signum() == 0 ? BigDecimal.valueOf(0) : expenseDiffrence.divide(totalPrevMonthExpense, 2, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));
        expenseDetail.setLastMonthChangeRate(Double.valueOf(expenseChangeRate.doubleValue()));

        expenseDetail.setOccured(prevMonthlyPaymentSummary.getTotalExpense());
      }
    }
    
    BigDecimal totalIncome = incomeDetail.getWaiting().add(incomeDetail.getOccured());
    BigDecimal totalExpense = expenseDetail.getWaiting();

    BigDecimal savingRate = totalIncome.compareTo(BigDecimal.ZERO) == 0 ? 
      BigDecimal.valueOf(0) :
        (
          totalExpense.divide(totalIncome, 2, RoundingMode.HALF_UP)
          .subtract(BigDecimal.valueOf(1))
        )
        .multiply(BigDecimal.valueOf(-1))
        .multiply(BigDecimal.valueOf(100));

    QuickViewResponseDto quickViewResponse = new QuickViewResponseDto();
    quickViewResponse.setTotalBalance(totalBalance);
    quickViewResponse.setIncome(incomeDetail);
    quickViewResponse.setExpense(expenseDetail);
    quickViewResponse.setSavingRate(Double.parseDouble(savingRate.toString()));

    List<TransactionStatuses> transactionStatuses = new ArrayList<TransactionStatuses>(
      List.of(TransactionStatuses.PARTIAL, TransactionStatuses.PENDING)
    );
    List<TransactionTypes> transactionTypes = new ArrayList<TransactionTypes>(
      List.of(TransactionTypes.DEBT, TransactionTypes.PAYMENT)
    );
    Long waitingInstallments = transactionRepository.findWaitingTransactions(userId, transactionStatuses, transactionTypes);
    quickViewResponse.setWaitingInstallments(Integer.valueOf(waitingInstallments.toString()));

    return quickViewResponse;
  }
  
}
