package com.hakandincturk.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hakandincturk.services.abstracts.MonthlySummaryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MonthlySummeryScheduler {

  private final MonthlySummaryService monthlySummaryService;
  
  @Scheduled(cron = "0 0 2 1 * *") // Her ayın 1’inde saat 02:00
  public void generateMonthlySummaries(){
    monthlySummaryService.generateMonthlySummariesForAllUsers();
  }

}
