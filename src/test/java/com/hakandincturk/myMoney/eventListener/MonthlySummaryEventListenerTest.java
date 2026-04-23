package com.hakandincturk.myMoney.eventListener;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.events.InstallmentPaidEvent;
import com.hakandincturk.eventListeners.MonthlySummaryEventListener;
import com.hakandincturk.models.Users;
import com.hakandincturk.services.abstracts.MonthlySummaryService;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryEventListenerTest {

  @InjectMocks
  private MonthlySummaryEventListener listener;

  @Mock
  private MonthlySummaryService monthlySummaryService;

  @Test
  @DisplayName("InstallmentPaidEvent alındığında aylık özet kaydedilmeli")
  void handlePayInstallmentEvent_shouldCallSaveForSpecificMonth() {
    Users user = new Users();
    user.setId(1L);

    InstallmentPaidEvent event = new InstallmentPaidEvent(user, 2025, 6);

    listener.handlePayInstallmentEvent(event);

    verify(monthlySummaryService).saveUserMonthlySummaryForSpecificMonth(user, 2025, 6);
  }

  @Test
  @DisplayName("Farklı ay/yıl parametreleri ile doğru çağrı yapılmalı")
  void handlePayInstallmentEvent_shouldPassCorrectYearAndMonth() {
    Users user = new Users();
    user.setId(2L);

    InstallmentPaidEvent event = new InstallmentPaidEvent(user, 2024, 12);

    listener.handlePayInstallmentEvent(event);

    verify(monthlySummaryService).saveUserMonthlySummaryForSpecificMonth(user, 2024, 12);
  }
}
