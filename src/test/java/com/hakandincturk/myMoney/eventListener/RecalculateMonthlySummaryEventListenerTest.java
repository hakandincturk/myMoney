package com.hakandincturk.myMoney.eventListener;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.events.InstallmentsPaidEvent;
import com.hakandincturk.core.events.TransactionCreatedEvent;
import com.hakandincturk.eventListeners.RecalculateMonthlySummaryEventListener;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;
import com.hakandincturk.services.abstracts.RecalculateMonthlySummaryService;

@ExtendWith(MockitoExtension.class)
class RecalculateMonthlySummaryEventListenerTest {

  @InjectMocks
  private RecalculateMonthlySummaryEventListener listener;

  @Mock
  private RecalculateMonthlySummaryService recalculateMonthlySummaryService;

  @Test
  @DisplayName("InstallmentsPaidEvent alındığında recalculate çağrılmalı")
  void handleInstallmentsPayment_shouldCallRecalculate() {
    Users user = new Users();
    user.setId(1L);

    Installment installment = new Installment();
    installment.setId(1L);

    LocalDate paidDate = LocalDate.of(2025, 6, 15);

    InstallmentsPaidEvent event = new InstallmentsPaidEvent(user, List.of(installment), paidDate);

    listener.handleInstallmentsPayment(event);

    verify(recalculateMonthlySummaryService).reCalculteAfterInstallmentPayment(user, List.of(installment), paidDate);
  }

  @Test
  @DisplayName("TransactionCreatedEvent alındığında recalculate çağrılmalı")
  void handleTransactionCreatedEvent_shouldCallRecalculate() {
    Transaction transaction = new Transaction();
    transaction.setId(100L);

    TransactionCreatedEvent event = new TransactionCreatedEvent(transaction);

    listener.handleTransactionCreatedEvent(event);

    verify(recalculateMonthlySummaryService).reCalculateAfterTransactionCreate(transaction);
  }
}
