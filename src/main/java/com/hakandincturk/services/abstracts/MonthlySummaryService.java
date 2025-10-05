package com.hakandincturk.services.abstracts;

import com.hakandincturk.dtos.monthlySummery.request.BackFillMonthlySummeriesRequest;
import com.hakandincturk.models.MonthlySummary;
import com.hakandincturk.models.Users;

public interface MonthlySummaryService {
  void fillForAllUsers();
  void fillForUsers(BackFillMonthlySummeriesRequest body);
  void saveUserMonthlySummaryForSpecificMonth(Users user, int year, int month);
  void generateMonthlySummariesForAllUsers();
  MonthlySummary calculateUserMonthlySummaryForSpecificMonthByTransactionDate(Users user, int year, int month);
  MonthlySummary calculateUserMonthlySummaryForSpecificMonthByPaidDate(Users user, int year, int month);
}
