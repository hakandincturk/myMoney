package com.hakandincturk.factories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hakandincturk.core.enums.MonthlySummeryTypes;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Users;

@Component
public class MonthlySummeryFactory {

  public MonthlySummary calculateUserMonthlySummaryForSpecificMonthByTransactionDate(Users user, List<Installment> thisMonthInstallments, List<Installment> nextMonthInstallments, int year, int month){

    BigDecimal thisMonthIncome = thisMonthInstallments.stream()
      .filter(this::isPaidInstallment)
      .filter(this::isIncomeInstallment)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal thisMonthWaitingIncome = thisMonthInstallments.stream()
      .filter(this::isUnpaidInstallment)
      .filter(this::isIncomeInstallment)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal thisMonthExpense = thisMonthInstallments.stream()
      .filter(this::isPaidInstallment)
      .filter(installment -> installment.getTransaction().getType() == TransactionTypes.DEBT)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal thisMonthWaitingExpense = thisMonthInstallments.stream()
      .filter(this::isUnpaidInstallment)
      .filter(installment -> installment.getTransaction().getType() == TransactionTypes.DEBT)
      // .filter(this::isExpenseInstallment)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal nextMonthWaitingExpense = nextMonthInstallments.stream()
      .filter(this::isUnpaidInstallment)
      .filter(installment -> installment.getTransaction().getType() == TransactionTypes.PAYMENT)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalWaitingExpense = thisMonthWaitingExpense.add(nextMonthWaitingExpense);

    MonthlySummary monthlySummary = new MonthlySummary(
      user,
      year,
      month,
      thisMonthIncome,
      thisMonthExpense,
      thisMonthWaitingIncome,
      totalWaitingExpense,
      MonthlySummeryTypes.TRANSACTION,
      LocalDate.of(year, month, 1)
    );
    return monthlySummary;
  }

  public MonthlySummary calculateUserMonthlySummaryForSpecificMonthByPaidDate(Users user, List<Installment> installments, int year, int month){
    
    BigDecimal totalIncomeForPaidDate = installments.stream()
      .filter(this::isIncomeInstallment)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalExpenseForPaidDate = installments.stream()
      .filter(this::isExpenseInstallment)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalWaitingIncome = installments.stream()
      .filter(this::isUnpaidInstallment)
      .filter(this::isIncomeInstallment)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalWaitingExpense = installments.stream()
      .filter(this::isUnpaidInstallment)
      .filter(this::isExpenseInstallment)
      .map(Installment::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    MonthlySummary monthlySummary = new MonthlySummary(
      user,
      year,
      month,
      totalIncomeForPaidDate,
      totalExpenseForPaidDate,
      totalWaitingIncome,
      totalWaitingExpense,
      MonthlySummeryTypes.PAYMENT,
      LocalDate.of(year, month, 1)
    );

    return monthlySummary;
  }
 
  private boolean isIncomeInstallment(Installment installment){
    return installment.getTransaction().getType() == TransactionTypes.CREDIT || installment.getTransaction().getType() == TransactionTypes.COLLECTION;
  }

  private boolean isExpenseInstallment(Installment installment){
    return installment.getTransaction().getType() == TransactionTypes.DEBT || installment.getTransaction().getType() == TransactionTypes.PAYMENT;
  }

  private boolean isPaidInstallment(Installment installment){
    return installment.isPaid();
  }

  private boolean isUnpaidInstallment(Installment installment){
    return installment.isPaid() == false;
  }
}
