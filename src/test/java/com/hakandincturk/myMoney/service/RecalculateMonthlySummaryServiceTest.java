package com.hakandincturk.myMoney.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.events.InstallmentPaidEvent;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.MonthlySummaryRepository;
import com.hakandincturk.services.impl.RecalculateMonthlySummaryServiceImpl;

@ExtendWith(MockitoExtension.class)
class RecalculateMonthlySummaryServiceTest {

  @InjectMocks
  private RecalculateMonthlySummaryServiceImpl service;

  @Mock
  private MonthlySummaryRepository monthlySummaryRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Test
  @DisplayName("Taksit ödemesi sonrası etkilenen aylar için özet yeniden hesaplanmalı")
  void reCalculteAfterInstallmentPayment_shouldRecalculateAffectedMonths() {
    Users user = new Users();
    user.setId(1L);

    Installment installment = new Installment();
    installment.setDebtDate(LocalDate.of(2025, 6, 15));

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.DEBT);
    installment.setTransaction(transaction);

    LocalDate paidDate = LocalDate.of(2025, 6, 20);

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(anyLong(), anyInt(), anyInt()))
        .thenReturn(List.of());

    service.reCalculteAfterInstallmentPayment(user, List.of(installment), paidDate);

    verify(monthlySummaryRepository, atLeastOnce()).deleteAll(any());
    verify(eventPublisher, atLeastOnce()).publishEvent(any(InstallmentPaidEvent.class));
  }

  @Test
  @DisplayName("Farklı ayda ödeme yapıldığında birden fazla ay etkilenmeli")
  void reCalculteAfterInstallmentPayment_differentMonth_shouldAffectMultipleMonths() {
    Users user = new Users();
    user.setId(1L);

    Installment installment = new Installment();
    installment.setDebtDate(LocalDate.of(2025, 6, 15));

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.DEBT);
    installment.setTransaction(transaction);

    LocalDate paidDate = LocalDate.of(2025, 7, 10);

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(anyLong(), anyInt(), anyInt()))
        .thenReturn(List.of());

    service.reCalculteAfterInstallmentPayment(user, List.of(installment), paidDate);

    verify(eventPublisher, atLeast(2)).publishEvent(any(InstallmentPaidEvent.class));
  }

  @Test
  @DisplayName("Transaction oluşturulduğunda etkilenen aylar hesaplanmalı")
  void reCalculateAfterTransactionCreate_shouldRecalculateAffectedMonths() {
    Users user = new Users();
    user.setId(1L);

    Installment i1 = new Installment();
    i1.setDebtDate(LocalDate.of(2025, 6, 1));
    Installment i2 = new Installment();
    i2.setDebtDate(LocalDate.of(2025, 7, 1));

    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setType(TransactionTypes.DEBT);
    transaction.setInstallments(List.of(i1, i2));

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(anyLong(), anyInt(), anyInt()))
        .thenReturn(List.of());

    service.reCalculateAfterTransactionCreate(transaction);

    verify(eventPublisher, atLeastOnce()).publishEvent(any(InstallmentPaidEvent.class));
  }

  @Test
  @DisplayName("PAYMENT tipi transaction oluşturulduğunda bir önceki ay da etkilenmeli")
  void reCalculateAfterTransactionCreate_payment_shouldAffectPreviousMonth() {
    Users user = new Users();
    user.setId(1L);

    Installment i1 = new Installment();
    i1.setDebtDate(LocalDate.of(2025, 6, 1));

    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setType(TransactionTypes.PAYMENT);
    transaction.setInstallments(List.of(i1));

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(anyLong(), anyInt(), anyInt()))
        .thenReturn(List.of());

    service.reCalculateAfterTransactionCreate(transaction);

    verify(eventPublisher, atLeast(2)).publishEvent(any(InstallmentPaidEvent.class));
  }

  @Test
  @DisplayName("Eski aylık özetler temizlenmeli")
  void reCalculateAfterTransactionCreate_shouldClearOldSummaries() {
    Users user = new Users();
    user.setId(1L);

    MonthlySummary existingSummary = new MonthlySummary();
    existingSummary.setId(1L);

    Installment i1 = new Installment();
    i1.setDebtDate(LocalDate.of(2025, 6, 1));

    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setType(TransactionTypes.DEBT);
    transaction.setInstallments(List.of(i1));

    when(monthlySummaryRepository.findByUser_IdAndYearAndMonthAndIsRemovedFalse(1L, 2025, 6))
        .thenReturn(List.of(existingSummary));

    service.reCalculateAfterTransactionCreate(transaction);

    verify(monthlySummaryRepository).deleteAll(List.of(existingSummary));
  }
}
