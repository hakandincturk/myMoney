package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
import com.hakandincturk.webapi.controllers.impl.ReportControllerImpl;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

  @InjectMocks
  private ReportControllerImpl controller;

  @Mock
  private ReportService reportService;

  private static final Long USER_ID = 1L;

  @BeforeEach
  void setUpSecurity() {
    JwtAuthentication auth = new JwtAuthentication("test@test.com", null, List.of(), USER_ID);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Özet - başarılı")
  void summary_shouldReturnSuccess() {
    ReportSummaryRequestDto params = new ReportSummaryRequestDto(2026, 6);
    when(reportService.summary(eq(USER_ID), any())).thenReturn(new ReportSummaryResponseDto());

    ApiResponse<ReportSummaryResponseDto> response = controller.summary(params);

    assertTrue(response.isType());
    assertNotNull(response.getData());
  }

  @Test
  @DisplayName("Özet - auth başarısız")
  void summary_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass"));

    ApiResponse<ReportSummaryResponseDto> response = controller.summary(new ReportSummaryRequestDto(2026, 6));

    assertFalse(response.isType());
    verifyNoInteractions(reportService);
  }

  @Test
  @DisplayName("Zaman serisi - başarılı")
  void timeline_shouldReturnSuccess() {
    when(reportService.timeline(eq(USER_ID), any())).thenReturn(new ReportTimelineResponseDto());

    ApiResponse<ReportTimelineResponseDto> response = controller.timeline(new ReportTimelineRequestDto(2026, 6, 6, 6));

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Etiket kırılımı - başarılı")
  void tagBreakdown_shouldReturnSuccess() {
    when(reportService.tagBreakdown(eq(USER_ID), any())).thenReturn(new ReportTagBreakdownResponseDto());

    ApiResponse<ReportTagBreakdownResponseDto> response = controller.tagBreakdown(new ReportTagBreakdownRequestDto());

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("En büyük kalemler - başarılı")
  void topExpenses_shouldReturnSuccess() {
    when(reportService.topExpenses(eq(USER_ID), any())).thenReturn(new ReportTopExpensesResponseDto());

    ApiResponse<ReportTopExpensesResponseDto> response = controller.topExpenses(new ReportTopExpensesRequestDto());

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Tekrar edenler - başarılı")
  void recurring_shouldReturnSuccess() {
    when(reportService.recurring(eq(USER_ID), any())).thenReturn(new ReportRecurringResponseDto());

    ApiResponse<ReportRecurringResponseDto> response = controller.recurring(new ReportRecurringRequestDto());

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Tekrar edenler - auth başarısız")
  void recurring_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass"));

    ApiResponse<ReportRecurringResponseDto> response = controller.recurring(new ReportRecurringRequestDto());

    assertFalse(response.isType());
  }
}
