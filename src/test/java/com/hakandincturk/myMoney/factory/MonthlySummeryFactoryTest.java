package com.hakandincturk.myMoney.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.enums.MonthlySummeryTypes;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.factories.MonthlySummeryFactory;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;

class MonthlySummeryFactoryTest {

  private final MonthlySummeryFactory factory = new MonthlySummeryFactory();

  private Installment createInstallment(TransactionTypes type, BigDecimal amount, boolean isPaid) {
    return createInstallment(type, amount, isPaid, InstallmentStatuses.ACTIVE);
  }

  private Installment createInstallment(TransactionTypes type, BigDecimal amount, boolean isPaid, InstallmentStatuses status) {
    Transaction transaction = new Transaction();
    transaction.setType(type);

    Installment installment = new Installment();
    installment.setTransaction(transaction);
    installment.setAmount(amount);
    installment.setPaid(isPaid);
    installment.setStatus(status);
    return installment;
  }

  @Test
  @DisplayName("Transaction tarihine göre aylık özet doğru hesaplanmalı")
  void calculateByTransactionDate_shouldCalculateCorrectly() {
    Users user = new Users();
    user.setId(1L);

    List<Installment> thisMonth = List.of(
        createInstallment(TransactionTypes.CREDIT, BigDecimal.valueOf(5000), true),
        createInstallment(TransactionTypes.DEBT, BigDecimal.valueOf(2000), true),
        createInstallment(TransactionTypes.COLLECTION, BigDecimal.valueOf(1000), false),
        createInstallment(TransactionTypes.PAYMENT, BigDecimal.valueOf(500), false)
    );

    List<Installment> nextMonth = List.of();

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        user, thisMonth, nextMonth, 2025, 6);

    assertEquals(BigDecimal.valueOf(5000), result.getTotalIncome());
    assertEquals(BigDecimal.valueOf(2000), result.getTotalExpense());
    assertEquals(BigDecimal.valueOf(1000), result.getTotalWaitingIncome());
    assertEquals(BigDecimal.valueOf(500), result.getTotalWaitingExpense());
    assertEquals(MonthlySummeryTypes.TRANSACTION, result.getType());
    assertEquals(2025, result.getYear());
    assertEquals(6, result.getMonth());
    assertEquals(user, result.getUser());
  }

  @Test
  @DisplayName("Boş taksit listesi ile sıfır değerler döndürülmeli")
  void calculateByTransactionDate_emptyList_shouldReturnZeros() {
    Users user = new Users();
    user.setId(1L);

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        user, List.of(), List.of(), 2025, 1);

    assertEquals(BigDecimal.ZERO, result.getTotalIncome());
    assertEquals(BigDecimal.ZERO, result.getTotalExpense());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingIncome());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingExpense());
  }

  @Test
  @DisplayName("Ödeme tarihine göre aylık özet doğru hesaplanmalı")
  void calculateByPaidDate_shouldCalculateCorrectly() {
    Users user = new Users();
    user.setId(1L);

    List<Installment> installments = List.of(
        createInstallment(TransactionTypes.COLLECTION, BigDecimal.valueOf(3000), true),
        createInstallment(TransactionTypes.PAYMENT, BigDecimal.valueOf(1500), true),
        createInstallment(TransactionTypes.CREDIT, BigDecimal.valueOf(500), false),
        createInstallment(TransactionTypes.DEBT, BigDecimal.valueOf(200), false)
    );

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(
        user, installments, 2025, 3);

    assertEquals(BigDecimal.valueOf(3500), result.getTotalIncome());
    assertEquals(BigDecimal.valueOf(1700), result.getTotalExpense());
    assertEquals(BigDecimal.valueOf(500), result.getTotalWaitingIncome());
    assertEquals(BigDecimal.valueOf(200), result.getTotalWaitingExpense());
    assertEquals(MonthlySummeryTypes.PAYMENT, result.getType());
    assertEquals(LocalDate.of(2025, 3, 1), result.getSummaryDate());
  }

  @Test
  @DisplayName("Sadece gelir taksitleri olduğunda gider sıfır olmalı")
  void calculateByTransactionDate_onlyIncome_shouldHaveZeroExpense() {
    Users user = new Users();
    user.setId(1L);

    List<Installment> thisMonth = List.of(
        createInstallment(TransactionTypes.CREDIT, BigDecimal.valueOf(1000), true),
        createInstallment(TransactionTypes.COLLECTION, BigDecimal.valueOf(2000), true)
    );

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        user, thisMonth, List.of(), 2025, 5);

    assertEquals(BigDecimal.valueOf(3000), result.getTotalIncome());
    assertEquals(BigDecimal.ZERO, result.getTotalExpense());
  }

  @Test
  @DisplayName("Sadece gider taksitleri olduğunda gelir sıfır olmalı")
  void calculateByTransactionDate_onlyExpense_shouldHaveZeroIncome() {
    Users user = new Users();
    user.setId(1L);

    List<Installment> thisMonth = List.of(
        createInstallment(TransactionTypes.DEBT, BigDecimal.valueOf(800), true),
        createInstallment(TransactionTypes.PAYMENT, BigDecimal.valueOf(400), true)
    );

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        user, thisMonth, List.of(), 2025, 5);

    assertEquals(BigDecimal.ZERO, result.getTotalIncome());
    assertEquals(BigDecimal.valueOf(1200), result.getTotalExpense());
  }

  @Test
  @DisplayName("SKIPPED taksitler transaction tarihine göre hesaplamadan hariç tutulmalı")
  void calculateByTransactionDate_shouldExcludeSkippedInstallments() {
    Users user = new Users();
    user.setId(1L);

    List<Installment> thisMonth = List.of(
        createInstallment(TransactionTypes.DEBT, BigDecimal.valueOf(1000), true, InstallmentStatuses.ACTIVE),
        createInstallment(TransactionTypes.DEBT, BigDecimal.valueOf(1000), false, InstallmentStatuses.SKIPPED),
        createInstallment(TransactionTypes.CREDIT, BigDecimal.valueOf(2000), true, InstallmentStatuses.ACTIVE),
        createInstallment(TransactionTypes.CREDIT, BigDecimal.valueOf(500), false, InstallmentStatuses.SKIPPED)
    );

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        user, thisMonth, List.of(), 2025, 6);

    assertEquals(BigDecimal.valueOf(2000), result.getTotalIncome());
    assertEquals(BigDecimal.valueOf(1000), result.getTotalExpense());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingIncome());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingExpense());
  }

  @Test
  @DisplayName("SKIPPED taksitler ödeme tarihine göre hesaplamadan hariç tutulmalı")
  void calculateByPaidDate_shouldExcludeSkippedInstallments() {
    Users user = new Users();
    user.setId(1L);

    List<Installment> installments = List.of(
        createInstallment(TransactionTypes.COLLECTION, BigDecimal.valueOf(3000), true, InstallmentStatuses.ACTIVE),
        createInstallment(TransactionTypes.COLLECTION, BigDecimal.valueOf(1000), true, InstallmentStatuses.SKIPPED),
        createInstallment(TransactionTypes.PAYMENT, BigDecimal.valueOf(1500), true, InstallmentStatuses.ACTIVE),
        createInstallment(TransactionTypes.DEBT, BigDecimal.valueOf(800), false, InstallmentStatuses.SKIPPED)
    );

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(
        user, installments, 2025, 3);

    assertEquals(BigDecimal.valueOf(3000), result.getTotalIncome());
    assertEquals(BigDecimal.valueOf(1500), result.getTotalExpense());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingIncome());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingExpense());
  }

  @Test
  @DisplayName("Tüm taksitler SKIPPED olduğunda sıfır değerler döndürülmeli")
  void calculateByTransactionDate_allSkipped_shouldReturnZeros() {
    Users user = new Users();
    user.setId(1L);

    List<Installment> thisMonth = List.of(
        createInstallment(TransactionTypes.DEBT, BigDecimal.valueOf(1000), true, InstallmentStatuses.SKIPPED),
        createInstallment(TransactionTypes.CREDIT, BigDecimal.valueOf(2000), false, InstallmentStatuses.SKIPPED)
    );

    MonthlySummary result = factory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        user, thisMonth, List.of(), 2025, 6);

    assertEquals(BigDecimal.ZERO, result.getTotalIncome());
    assertEquals(BigDecimal.ZERO, result.getTotalExpense());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingIncome());
    assertEquals(BigDecimal.ZERO, result.getTotalWaitingExpense());
  }
}
