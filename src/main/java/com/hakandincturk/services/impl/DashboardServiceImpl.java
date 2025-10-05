package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.MonthlySummeryTypes;
import com.hakandincturk.dtos.dashboard.response.QuickViewIncomeAndExpenseDetailDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.services.abstracts.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private final AccountRepository accountRepository;
  private final MonthlySummaryRepository monthlySummaryRepository;

  @Override
  public QuickViewResponseDto quickViewResponse(Long userId) {

    List<AccountTypes> accountTypes = List.of(AccountTypes.BANK, AccountTypes.CASH);
    BigDecimal totalBalance = accountRepository.sumBalanceByUserIdAndTypes(userId, accountTypes);

    QuickViewIncomeAndExpenseDetailDto incomeDetail = new QuickViewIncomeAndExpenseDetailDto();
    QuickViewIncomeAndExpenseDetailDto expenseDetail = new QuickViewIncomeAndExpenseDetailDto();

    LocalDateTime currentDate = LocalDateTime.now();

    // Optional<MonthlySummary> dd = monthlySummaryRepository.findBy(135L);
    Optional<MonthlySummary> dbMonthlyTransactionSummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(userId, currentDate.getYear(), currentDate.getMonthValue(), MonthlySummeryTypes.TRANSACTION);
    Optional<MonthlySummary> dbMonthlPaymentSummary = monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(userId, currentDate.getYear(), currentDate.getMonthValue(), MonthlySummeryTypes.PAYMENT);

    if(dbMonthlyTransactionSummary.isEmpty()){
      incomeDetail.setOccured(BigDecimal.valueOf(0));
      incomeDetail.setWaiting(BigDecimal.valueOf(0));
      incomeDetail.setLastMonthChangeRate(Double.valueOf(0));

      expenseDetail.setOccured(BigDecimal.valueOf(0));
      expenseDetail.setWaiting(BigDecimal.valueOf(0));
      expenseDetail.setLastMonthChangeRate(Double.valueOf(0));
    }
    else {
      MonthlySummary monthlPaymentSummary = dbMonthlyTransactionSummary.get();
      incomeDetail.setOccured(monthlPaymentSummary.getTotalIncome());
      incomeDetail.setWaiting(monthlPaymentSummary.getTotalWaitingIncome());
      
      expenseDetail.setOccured(monthlPaymentSummary.getTotalExpense());
      expenseDetail.setWaiting(monthlPaymentSummary.getTotalWaitingExpense());
    }
    
    BigDecimal totalIncome = incomeDetail.getWaiting().add(incomeDetail.getOccured());
    BigDecimal totalExpense = expenseDetail.getWaiting().add(expenseDetail.getOccured());
    BigDecimal savingRate = !totalIncome.equals(BigDecimal.valueOf(0)) ? ((totalIncome.subtract(totalExpense)).divide(totalIncome,  4, RoundingMode.HALF_EVEN)).multiply(BigDecimal.valueOf(100)) : BigDecimal.valueOf(0);

    QuickViewResponseDto quickViewResponse = new QuickViewResponseDto();
    quickViewResponse.setTotalBalance(totalBalance);
    quickViewResponse.setIncome(incomeDetail);
    quickViewResponse.setExpense(expenseDetail);
    quickViewResponse.setSavingRate(Double.parseDouble(savingRate.toString()));
    quickViewResponse.setWaitingInstallments(0);

    return quickViewResponse;
  }
  
}
