package com.hakandincturk.eventListeners;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.hakandincturk.core.events.PayInstallmentEvent;
import com.hakandincturk.services.abstracts.MonthlySummaryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MonthlySummaryEventListener {

  private final MonthlySummaryService monthlySummaryService;
  
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handlePayInstallmentEvent(PayInstallmentEvent event) {
    monthlySummaryService.saveUserMonthlySummaryForSpecificMonth(event.getUser(), event.getYear(), event.getMonth());
  }

}
