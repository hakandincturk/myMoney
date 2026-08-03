package com.hakandincturk.services.abstracts;

import com.hakandincturk.dtos.report.request.ReportRecurringRequestDto;
import com.hakandincturk.dtos.report.request.ReportSummaryRequestDto;
import com.hakandincturk.dtos.report.request.ReportTagBreakdownRequestDto;
import com.hakandincturk.dtos.report.request.ReportTimelineRequestDto;
import com.hakandincturk.dtos.report.request.ReportTopExpensesRequestDto;
import com.hakandincturk.dtos.report.response.ReportRecurringResponseDto;
import com.hakandincturk.dtos.report.response.ReportSummaryResponseDto;
import com.hakandincturk.dtos.report.response.ReportTagBreakdownResponseDto;
import com.hakandincturk.dtos.report.response.ReportTimelineResponseDto;
import com.hakandincturk.dtos.report.response.ReportTopExpensesResponseDto;

public interface ReportService {
  ReportSummaryResponseDto summary(Long userId, ReportSummaryRequestDto request);
  ReportTimelineResponseDto timeline(Long userId, ReportTimelineRequestDto request);
  ReportTagBreakdownResponseDto tagBreakdown(Long userId, ReportTagBreakdownRequestDto request);
  ReportTopExpensesResponseDto topExpenses(Long userId, ReportTopExpensesRequestDto request);
  ReportRecurringResponseDto recurring(Long userId, ReportRecurringRequestDto request);
}
