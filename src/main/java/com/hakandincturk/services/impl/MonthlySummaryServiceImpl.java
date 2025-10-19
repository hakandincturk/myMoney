package com.hakandincturk.services.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.monthlySummery.request.BackFillMonthlySummeriesRequest;
import com.hakandincturk.factories.MonthlySummeryFactory;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.services.abstracts.MonthlySummaryService;
import com.hakandincturk.services.rules.UserRules;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlySummaryServiceImpl implements MonthlySummaryService {

  private final InstallmentRepository installmentRepository;
  private final MonthlySummaryRepository monthlySummaryRepository;
  private final UserRepository userRepository;
  private final MonthlySummeryFactory monthlySummeryFactory;
  private final UserRules userRules;

  @Transactional
  public void fillForUsers(BackFillMonthlySummeriesRequest body){
    List<Users> dbUsers = userRules.checkAllUsersExistAndGet(body.getUserIds());
    for (Users user : dbUsers) {
      List<Installment> userInstallments = installmentRepository.findByTransaction_UserIdOrderByDebtDate(user.getId());
      if(userInstallments.isEmpty()){
        return;
      }

      Installment userFirstInstallment = userInstallments.getFirst();
      Installment userLastInstallment = userInstallments.getLast();

      YearMonth firstInstallmentDate = YearMonth.from(userFirstInstallment.getDebtDate());
      YearMonth lastInstallmentDate = YearMonth.from(userLastInstallment.getDebtDate());

      YearMonth startDate = firstInstallmentDate;
      do{
        int year = startDate.getYear();
        int month = startDate.getMonthValue();

        this.saveUserMonthlySummaryForSpecificMonth(user, year, month);

        startDate = startDate.plusMonths(1);
      }while(startDate.isBefore(lastInstallmentDate));
    }
  }

  @Transactional
  public void fillForAllUsers(){
  List<Users> dbUsers = userRepository.findAllByIsRemovedFalse();
  for (Users user : dbUsers) {
    List<Installment> dbUserFirstInstallemnt = installmentRepository.findByTransaction_UserIdOrderByDebtDate(user.getId());
    if(dbUserFirstInstallemnt.isEmpty()){
      continue;
    }

    Installment userFirstInstallment = dbUserFirstInstallemnt.getFirst();
    Installment userLastInstallment = dbUserFirstInstallemnt.getLast();

    YearMonth firstInstallmentDate = YearMonth.from(userFirstInstallment.getDebtDate());

  
    LocalDate lastInstallmentDate = userLastInstallment.getDebtDate() == null ? LocalDate.of(LocalDate.now().getYear(), 12, 30) : userLastInstallment.getDebtDate();
    YearMonth lastInstallmentDates = YearMonth.from(lastInstallmentDate);

    YearMonth startDate = firstInstallmentDate;
    do{
      int year = startDate.getYear();
      int month = startDate.getMonthValue();

      this.saveUserMonthlySummaryForSpecificMonth(user, year, month);

      startDate = startDate.plusMonths(1);
    }while(startDate.isBefore(lastInstallmentDates));
  }
}

  @Transactional
  public void generateMonthlySummariesForAllUsers(){
    List<Users> users = userRepository.findAllByIsRemovedFalse();
    YearMonth previousMonth = YearMonth.now().minusMonths(1);
    int year = previousMonth.atDay(1).getYear();
    int month = previousMonth.atDay(1).getMonthValue();

    for (Users user : users) {
      this.saveUserMonthlySummaryForSpecificMonth(user, year, month);
    }
  }

  @Transactional
  public void saveUserMonthlySummaryForSpecificMonth(Users user, int year, int month){
    MonthlySummary monthlySummaryByTransactionDate = this.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(user, year, month);
    MonthlySummary monthlySummaryByPaidDate = this.calculateUserMonthlySummaryForSpecificMonthByPaidDate(user, year, month);

    List<MonthlySummary> summaries = new ArrayList<>();
    summaries.add(monthlySummaryByTransactionDate);
    summaries.add(monthlySummaryByPaidDate);

    monthlySummaryRepository.saveAll(summaries);
  }

  @Override
  public MonthlySummary calculateUserMonthlySummaryForSpecificMonthByTransactionDate(Users user, int year, int month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
    
    LocalDate nextMonthStarDate = startDate.plusMonths(1);
    LocalDate nextMonthEndDate = YearMonth.of(nextMonthStarDate.getYear(), nextMonthStarDate.getMonthValue()).atEndOfMonth();

    List<TransactionTypes> nextMonthTransactionTypes = new ArrayList<TransactionTypes>(
      List.of(TransactionTypes.PAYMENT, TransactionTypes.CREDIT)
    );
    List<Installment> dbUserNextMonthPaymentsByTransactionDate = installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(user.getId(), nextMonthTransactionTypes, nextMonthStarDate, nextMonthEndDate);
    List<Installment> dbUserInstallmentsByTransactionDate = installmentRepository.findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(user.getId(), startDate, endDate);


    MonthlySummary monthlySummaryByTransactionDate = monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(user, dbUserInstallmentsByTransactionDate, dbUserNextMonthPaymentsByTransactionDate, year, month);

    return monthlySummaryByTransactionDate;
  }
  
  @Override
  public MonthlySummary calculateUserMonthlySummaryForSpecificMonthByPaidDate(Users user, int year, int month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
    List<Installment> dbUserInstallmentsByPaidDate = installmentRepository.findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(user.getId(), startDate, endDate);

    MonthlySummary monthlySummaryByPaidDate = monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(user, dbUserInstallmentsByPaidDate, year, month);
    return monthlySummaryByPaidDate;
  }

}
