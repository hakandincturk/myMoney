package com.hakandincturk.eventListeners;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.hakandincturk.core.events.InstallmentsPaidEvent;
import com.hakandincturk.core.events.TransactionCreatedEvent;
import com.hakandincturk.services.abstracts.RecalculateMonthlySummaryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecalculateMonthlySummaryEventListener {
  private final RecalculateMonthlySummaryService recalculateMonthlySummaryService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleInstallmentsPayment(InstallmentsPaidEvent event){
    recalculateMonthlySummaryService.reCalculteAfterInstallmentPayment(
      event.getInstallmentUser(),
      event.getInstallments(),
      event.getPaidDate()
      );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleTransactionCreatedEvent(TransactionCreatedEvent event){
    recalculateMonthlySummaryService.reCalculateAfterTransactionCreate(event.getTransaction());
  }
}
