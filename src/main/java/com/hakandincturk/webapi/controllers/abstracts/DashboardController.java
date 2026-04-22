package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.dashboard.request.TagSummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.TagSummaryResponseDto;
import com.hakandincturk.dtos.dashboard.response.IncomingInstallmentsResponseDto;
import com.hakandincturk.dtos.dashboard.response.LastTransactionsResponseDto;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;

public interface DashboardController {
  ApiResponse<QuickViewResponseDto> quickViewResponse();
  ApiResponse<MonthlyTrendResponseDto> monthlyTrend();
  ApiResponse<TagSummaryResponseDto> tagSummary(DashboardTagSummaryTypes type, DashboardTagSummarySumMode sumMode, TagSummaryRequestDto body);
  ApiResponse<LastTransactionsResponseDto> lastTransactions();
  ApiResponse<IncomingInstallmentsResponseDto> incomingInstallments();
}
