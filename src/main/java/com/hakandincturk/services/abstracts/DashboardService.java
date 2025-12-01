package com.hakandincturk.services.abstracts;

import com.hakandincturk.core.enums.DashboardCategorySummarySumMode;
import com.hakandincturk.core.enums.DashboardCategorySummaryTypes;
import com.hakandincturk.dtos.dashboard.request.CategorySummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.CategorySummaryResponseDto;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;

public interface DashboardService {
  QuickViewResponseDto quickViewResponse(Long userId);
  MonthlyTrendResponseDto monthlyTrend(Long userId);
  CategorySummaryResponseDto categorySummary(Long userId, DashboardCategorySummaryTypes type, DashboardCategorySummarySumMode sumMode,  CategorySummaryRequestDto body);
}
