package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.monthlySummery.request.BackFillMonthlySummeriesRequest;

public interface MonthlySummaryController {
  ApiResponse<?> fillForAllUsers();  
  ApiResponse<?> fillForUsers(BackFillMonthlySummeriesRequest body);  
}