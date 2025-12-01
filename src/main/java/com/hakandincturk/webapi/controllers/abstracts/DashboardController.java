package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.enums.DashboardCategorySummarySumMode;
import com.hakandincturk.core.enums.DashboardCategorySummaryTypes;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.dashboard.request.CategorySummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.CategorySummaryResponseDto;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;

public interface DashboardController {
  ApiResponse<QuickViewResponseDto> quickViewResponse();
  ApiResponse<MonthlyTrendResponseDto> monthlyTrend();
  ApiResponse<CategorySummaryResponseDto> categorySummary(DashboardCategorySummaryTypes type, DashboardCategorySummarySumMode sumMode, CategorySummaryRequestDto body);
}
