package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
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

public interface ReportController {
  ApiResponse<ReportSummaryResponseDto> summary(ReportSummaryRequestDto params);
  ApiResponse<ReportTimelineResponseDto> timeline(ReportTimelineRequestDto params);
  ApiResponse<ReportTagBreakdownResponseDto> tagBreakdown(ReportTagBreakdownRequestDto params);
  ApiResponse<ReportTopExpensesResponseDto> topExpenses(ReportTopExpensesRequestDto params);
  ApiResponse<ReportRecurringResponseDto> recurring(ReportRecurringRequestDto params);
}
