package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.enums.MonthlySummeryTypes;
import com.hakandincturk.factories.MonthlySummeryFactory;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.services.impl.MonthlySummaryServiceImpl;
import com.hakandincturk.services.rules.UserRules;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryServiceTest {

  @InjectMocks
  private MonthlySummaryServiceImpl monthlySummaryService;

  @Mock
  private InstallmentRepository installmentRepository;

  @Mock
  private MonthlySummaryRepository monthlySummaryRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MonthlySummeryFactory monthlySummeryFactory;

  @Mock
  private UserRules userRules;

  @Test
  @DisplayName("Belirli ay için aylık özet kaydedilmeli - kayıt yoksa yeni oluşturulmalı")
  void saveUserMonthlySummaryForSpecificMonth_shouldSaveBothTypes() {
    Users user = new Users();
    user.setId(1L);

    MonthlySummary transactionSummary = new MonthlySummary();
    transactionSummary.setType(MonthlySummeryTypes.TRANSACTION);
    MonthlySummary paymentSummary = new MonthlySummary();
    paymentSummary.setType(MonthlySummeryTypes.PAYMENT);

    when(installmentRepository.findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), any(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(
        eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        eq(1L), eq(2025), eq(6), any(MonthlySummeryTypes.class)))
        .thenReturn(Optional.empty());

    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        eq(user), any(), any(), eq(2025), eq(6)))
        .thenReturn(transactionSummary);
    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(
        eq(user), any(), eq(2025), eq(6)))
        .thenReturn(paymentSummary);

    monthlySummaryService.saveUserMonthlySummaryForSpecificMonth(user, 2025, 6);

    verify(monthlySummaryRepository).saveAll(argThat(list -> {
      List<MonthlySummary> summaries = (List<MonthlySummary>) list;
      return summaries.size() == 2;
    }));
  }

  @Test
  @DisplayName("Mevcut kayıt varsa yeni kayıt oluşturmak yerine mevcut kayıt güncellenmeli")
  void saveUserMonthlySummaryForSpecificMonth_shouldUpdateExistingRecords() {
    Users user = new Users();
    user.setId(1L);

    MonthlySummary existingTransaction = new MonthlySummary();
    existingTransaction.setId(100L);
    existingTransaction.setType(MonthlySummeryTypes.TRANSACTION);
    existingTransaction.setTotalIncome(BigDecimal.valueOf(1000));
    existingTransaction.setTotalExpense(BigDecimal.valueOf(500));
    existingTransaction.setTotalWaitingIncome(BigDecimal.valueOf(200));
    existingTransaction.setTotalWaitingExpense(BigDecimal.valueOf(100));

    MonthlySummary existingPayment = new MonthlySummary();
    existingPayment.setId(101L);
    existingPayment.setType(MonthlySummeryTypes.PAYMENT);
    existingPayment.setTotalIncome(BigDecimal.valueOf(800));
    existingPayment.setTotalExpense(BigDecimal.valueOf(400));
    existingPayment.setTotalWaitingIncome(BigDecimal.ZERO);
    existingPayment.setTotalWaitingExpense(BigDecimal.ZERO);

    MonthlySummary calculatedTransaction = new MonthlySummary();
    calculatedTransaction.setType(MonthlySummeryTypes.TRANSACTION);
    calculatedTransaction.setTotalIncome(BigDecimal.valueOf(1500));
    calculatedTransaction.setTotalExpense(BigDecimal.valueOf(700));
    calculatedTransaction.setTotalWaitingIncome(BigDecimal.valueOf(300));
    calculatedTransaction.setTotalWaitingExpense(BigDecimal.valueOf(150));

    MonthlySummary calculatedPayment = new MonthlySummary();
    calculatedPayment.setType(MonthlySummeryTypes.PAYMENT);
    calculatedPayment.setTotalIncome(BigDecimal.valueOf(1200));
    calculatedPayment.setTotalExpense(BigDecimal.valueOf(600));
    calculatedPayment.setTotalWaitingIncome(BigDecimal.ZERO);
    calculatedPayment.setTotalWaitingExpense(BigDecimal.ZERO);

    when(installmentRepository.findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), any(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(
        eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        1L, 2025, 6, MonthlySummeryTypes.TRANSACTION))
        .thenReturn(Optional.of(existingTransaction));
    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        1L, 2025, 6, MonthlySummeryTypes.PAYMENT))
        .thenReturn(Optional.of(existingPayment));

    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        eq(user), any(), any(), eq(2025), eq(6)))
        .thenReturn(calculatedTransaction);
    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(
        eq(user), any(), eq(2025), eq(6)))
        .thenReturn(calculatedPayment);

    monthlySummaryService.saveUserMonthlySummaryForSpecificMonth(user, 2025, 6);

    verify(monthlySummaryRepository).saveAll(argThat(list -> {
      List<MonthlySummary> summaries = (List<MonthlySummary>) list;
      return summaries.size() == 2
          && summaries.stream().allMatch(s -> s.getId() != null)
          && summaries.stream().anyMatch(s -> s.getId().equals(100L))
          && summaries.stream().anyMatch(s -> s.getId().equals(101L));
    }));

    assertEquals(BigDecimal.valueOf(1500), existingTransaction.getTotalIncome());
    assertEquals(BigDecimal.valueOf(700), existingTransaction.getTotalExpense());
    assertEquals(BigDecimal.valueOf(300), existingTransaction.getTotalWaitingIncome());
    assertEquals(BigDecimal.valueOf(150), existingTransaction.getTotalWaitingExpense());

    assertEquals(BigDecimal.valueOf(1200), existingPayment.getTotalIncome());
    assertEquals(BigDecimal.valueOf(600), existingPayment.getTotalExpense());
  }

  @Test
  @DisplayName("Upsert: mevcut kayıt varsa ID korunmalı, yeni kayıt oluşturulmamalı")
  void saveUserMonthlySummaryForSpecificMonth_shouldPreserveExistingId() {
    Users user = new Users();
    user.setId(1L);

    MonthlySummary existingTransaction = new MonthlySummary();
    existingTransaction.setId(50L);
    existingTransaction.setType(MonthlySummeryTypes.TRANSACTION);
    existingTransaction.setTotalIncome(BigDecimal.ZERO);
    existingTransaction.setTotalExpense(BigDecimal.ZERO);
    existingTransaction.setTotalWaitingIncome(BigDecimal.ZERO);
    existingTransaction.setTotalWaitingExpense(BigDecimal.ZERO);

    MonthlySummary calculatedTransaction = new MonthlySummary();
    calculatedTransaction.setType(MonthlySummeryTypes.TRANSACTION);
    calculatedTransaction.setTotalIncome(BigDecimal.valueOf(2000));
    calculatedTransaction.setTotalExpense(BigDecimal.valueOf(1000));
    calculatedTransaction.setTotalWaitingIncome(BigDecimal.valueOf(500));
    calculatedTransaction.setTotalWaitingExpense(BigDecimal.valueOf(250));

    MonthlySummary calculatedPayment = new MonthlySummary();
    calculatedPayment.setType(MonthlySummeryTypes.PAYMENT);
    calculatedPayment.setTotalIncome(BigDecimal.ZERO);
    calculatedPayment.setTotalExpense(BigDecimal.ZERO);
    calculatedPayment.setTotalWaitingIncome(BigDecimal.ZERO);
    calculatedPayment.setTotalWaitingExpense(BigDecimal.ZERO);

    when(installmentRepository.findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), any(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(
        eq(1L), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        1L, 2025, 6, MonthlySummeryTypes.TRANSACTION))
        .thenReturn(Optional.of(existingTransaction));
    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        1L, 2025, 6, MonthlySummeryTypes.PAYMENT))
        .thenReturn(Optional.empty());

    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        eq(user), any(), any(), eq(2025), eq(6)))
        .thenReturn(calculatedTransaction);
    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(
        eq(user), any(), eq(2025), eq(6)))
        .thenReturn(calculatedPayment);

    monthlySummaryService.saveUserMonthlySummaryForSpecificMonth(user, 2025, 6);

    verify(monthlySummaryRepository).saveAll(argThat(list -> {
      List<MonthlySummary> summaries = (List<MonthlySummary>) list;
      MonthlySummary savedTransaction = summaries.stream()
          .filter(s -> s.getId() != null && s.getId().equals(50L))
          .findFirst().orElse(null);
      MonthlySummary savedPayment = summaries.stream()
          .filter(s -> s.getId() == null)
          .findFirst().orElse(null);
      return summaries.size() == 2
          && savedTransaction != null
          && savedPayment != null;
    }));

    assertEquals(BigDecimal.valueOf(2000), existingTransaction.getTotalIncome());
    assertEquals(BigDecimal.valueOf(1000), existingTransaction.getTotalExpense());
  }

  @Test
  @DisplayName("Transaction tarihine göre aylık özet hesaplama doğru repository çağrıları yapmalı")
  void calculateByTransactionDate_shouldCallRepositoryCorrectly() {
    Users user = new Users();
    user.setId(1L);

    MonthlySummary summary = new MonthlySummary();

    when(installmentRepository.findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), eq(LocalDate.of(2025, 6, 1)), eq(LocalDate.of(2025, 6, 30))))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(
        eq(1L), any(), eq(LocalDate.of(2025, 7, 1)), eq(LocalDate.of(2025, 7, 31))))
        .thenReturn(List.of());

    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        eq(user), any(), any(), eq(2025), eq(6)))
        .thenReturn(summary);

    MonthlySummary result = monthlySummaryService.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(user, 2025, 6);

    assertNotNull(result);
    verify(installmentRepository).findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(1L, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));
  }

  @Test
  @DisplayName("Ödeme tarihine göre aylık özet hesaplama")
  void calculateByPaidDate_shouldCallRepositoryAndFactory() {
    Users user = new Users();
    user.setId(1L);

    MonthlySummary summary = new MonthlySummary();

    when(installmentRepository.findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(
        eq(1L), eq(LocalDate.of(2025, 3, 1)), eq(LocalDate.of(2025, 3, 31))))
        .thenReturn(List.of());
    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(
        eq(user), any(), eq(2025), eq(3)))
        .thenReturn(summary);

    MonthlySummary result = monthlySummaryService.calculateUserMonthlySummaryForSpecificMonthByPaidDate(user, 2025, 3);

    assertNotNull(result);
    verify(installmentRepository).findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(1L, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31));
  }

  @Test
  @DisplayName("Tüm aktif kullanıcılar için aylık özet oluşturma")
  void generateMonthlySummariesForAllUsers_shouldProcessAllUsers() {
    Users user1 = new Users();
    user1.setId(1L);
    Users user2 = new Users();
    user2.setId(2L);

    when(userRepository.findAllByIsRemovedFalse()).thenReturn(List.of(user1, user2));

    when(installmentRepository.findByTransaction_UserIdAndDebtDateBetweenAndIsRemovedFalse(
        anyLong(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndTransactionTypeInAndDebtDateBetweenAndIsRemovedFalse(
        anyLong(), any(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());
    when(installmentRepository.findByTransaction_UserIdAndPaidDateBetweenAndIsPaidTrueAndIsRemovedFalse(
        anyLong(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of());

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndTypeAndIsRemovedFalse(
        anyLong(), anyInt(), anyInt(), any(MonthlySummeryTypes.class)))
        .thenReturn(Optional.empty());

    MonthlySummary summary = new MonthlySummary();
    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByTransactionDate(
        any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(summary);
    when(monthlySummeryFactory.calculateUserMonthlySummaryForSpecificMonthByPaidDate(
        any(), any(), anyInt(), anyInt()))
        .thenReturn(summary);

    monthlySummaryService.generateMonthlySummariesForAllUsers();

    verify(monthlySummaryRepository, times(2)).saveAll(any());
  }
}
