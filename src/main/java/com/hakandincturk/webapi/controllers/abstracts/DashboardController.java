package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrend;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;

public interface DashboardController {
  ApiResponse<QuickViewResponseDto> quickViewResponse();
  ApiResponse<MonthlyTrend> monthlyTrend();
}
