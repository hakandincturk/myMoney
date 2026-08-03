package com.hakandincturk.webapi.controllers.impl;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.ReportService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.ReportController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/report")
@RequiredArgsConstructor
@Tag(name = "Report", description = "Rapor işlemleri")
public class ReportControllerImpl extends BaseController implements ReportController {

  private final ReportService reportService;

  @Override
  @GetMapping(value = "/summary")
  @Operation(summary = "Report Summary", description = "Seçilen ayı önceki ve sonraki ayla karşılaştıran özet verilerini getirir")
  public ApiResponse<ReportSummaryResponseDto> summary(@ParameterObject @Valid ReportSummaryRequestDto params) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Rapor özeti getirildi", reportService.summary(userId, params));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/timeline")
  @Operation(summary = "Report Timeline", description = "Geçmiş ve gelecek aylara ait gelir/gider serisini getirir")
  public ApiResponse<ReportTimelineResponseDto> timeline(@ParameterObject @Valid ReportTimelineRequestDto params) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Rapor zaman serisi getirildi", reportService.timeline(userId, params));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/tag-breakdown")
  @Operation(summary = "Report Tag Breakdown", description = "Etiket bazlı kırılımı ve önceki ay karşılaştırmasını getirir")
  public ApiResponse<ReportTagBreakdownResponseDto> tagBreakdown(@ParameterObject @Valid ReportTagBreakdownRequestDto params) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Etiket bazlı kırılım getirildi", reportService.tagBreakdown(userId, params));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/top-expenses")
  @Operation(summary = "Report Top Expenses", description = "Seçilen ayın en büyük kalemlerini getirir")
  public ApiResponse<ReportTopExpensesResponseDto> topExpenses(@ParameterObject @Valid ReportTopExpensesRequestDto params) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Ayın en büyük kalemleri getirildi", reportService.topExpenses(userId, params));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/recurring")
  @Operation(summary = "Report Recurring Expenses", description = "Tekrar eden harcamaları ve aylık sabit gider yükünü getirir")
  public ApiResponse<ReportRecurringResponseDto> recurring(@ParameterObject @Valid ReportRecurringRequestDto params) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Tekrar eden harcamalar getirildi", reportService.recurring(userId, params));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

}
