package com.hakandincturk.services.abstracts;

import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.dtos.dashboard.request.TagSummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.TagSummaryResponseDto;
import com.hakandincturk.dtos.dashboard.response.IncomingInstallmentsResponseDto;
import com.hakandincturk.dtos.dashboard.response.LastTransactionsResponseDto;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;

public interface DashboardService {
  QuickViewResponseDto quickViewResponse(Long userId);
  MonthlyTrendResponseDto monthlyTrend(Long userId);
  TagSummaryResponseDto tagSummary(Long userId, DashboardTagSummaryTypes type, DashboardTagSummarySumMode sumMode,  TagSummaryRequestDto body);

  LastTransactionsResponseDto lastTransactions(Long userId);

  IncomingInstallmentsResponseDto incomingInstallments(Long userId);

}
