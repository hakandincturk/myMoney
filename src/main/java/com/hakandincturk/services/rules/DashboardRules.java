package com.hakandincturk.services.rules;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.hakandincturk.dtos.dashboard.request.CategorySummaryRequestDto;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardRules {
  public void categorySummaryDatesControl(CategorySummaryRequestDto body){
    if(body.getEndDate().isBefore(body.getStartDate())){
      throw new ValidationException("Bitiş tarihi, başlangıç tarihinden daha önce olamaz");
    }
  }

  public void categorySummaryDatesOnly1MonthOr1Year(CategorySummaryRequestDto body){
    if(
      !body.getStartDate().equals(LocalDate.of(body.getEndDate().getYear(), 1, 1)) &&
      !body.getStartDate().equals(LocalDate.of(body.getEndDate().getYear(), body.getStartDate().getMonthValue(), 1)) 
    ){
      throw new ValidationException("Başlangıç ve bitiş tarihleri aylık veya yıllık olmalı");
    }
  };
}
