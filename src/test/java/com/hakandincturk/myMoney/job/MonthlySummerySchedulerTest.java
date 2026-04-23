package com.hakandincturk.myMoney.job;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.jobs.MonthlySummeryScheduler;
import com.hakandincturk.services.abstracts.MonthlySummaryService;

@ExtendWith(MockitoExtension.class)
class MonthlySummerySchedulerTest {

  @InjectMocks
  private MonthlySummeryScheduler scheduler;

  @Mock
  private MonthlySummaryService monthlySummaryService;

  @Test
  @DisplayName("Scheduler çalıştığında tüm kullanıcılar için aylık özet oluşturulmalı")
  void generateMonthlySummaries_shouldCallService() {
    scheduler.generateMonthlySummaries();

    verify(monthlySummaryService).generateMonthlySummariesForAllUsers();
  }
}
