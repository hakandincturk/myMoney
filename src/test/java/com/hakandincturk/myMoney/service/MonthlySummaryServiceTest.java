package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
  @DisplayName("Belirli ay için aylık özet kaydedilmeli")
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
