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
import org.springframework.security.core.context.SecurityContextHolder;

import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.dashboard.request.TagSummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.IncomingInstallmentsResponseDto;
import com.hakandincturk.dtos.dashboard.response.LastTransactionsResponseDto;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.dtos.dashboard.response.TagSummaryResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.DashboardService;
import com.hakandincturk.webapi.controllers.impl.DashboardControllerImpl;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

  @InjectMocks
  private DashboardControllerImpl controller;

  @Mock
  private DashboardService dashboardService;

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
  @DisplayName("QuickView - başarılı")
  void quickViewResponse_shouldReturnSuccess() {
    QuickViewResponseDto dto = new QuickViewResponseDto();
    when(dashboardService.quickViewResponse(USER_ID)).thenReturn(dto);

    ApiResponse<QuickViewResponseDto> response = controller.quickViewResponse();

    assertTrue(response.isType());
    assertNotNull(response.getData());
  }

  @Test
  @DisplayName("QuickView - auth başarısız")
  void quickViewResponse_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass"));

    ApiResponse<QuickViewResponseDto> response = controller.quickViewResponse();

    assertFalse(response.isType());
  }

  @Test
  @DisplayName("Aylık trend - başarılı")
  void monthlyTrend_shouldReturnSuccess() {
    MonthlyTrendResponseDto dto = new MonthlyTrendResponseDto();
    when(dashboardService.monthlyTrend(USER_ID)).thenReturn(dto);

    ApiResponse<MonthlyTrendResponseDto> response = controller.monthlyTrend();

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Tag özeti - başarılı")
  void tagSummary_shouldReturnSuccess() {
    TagSummaryResponseDto dto = new TagSummaryResponseDto();
    when(dashboardService.tagSummary(eq(USER_ID), any(), any(), any())).thenReturn(dto);

    TagSummaryRequestDto body = new TagSummaryRequestDto();
    ApiResponse<TagSummaryResponseDto> response = controller.tagSummary(
        DashboardTagSummaryTypes.MONTHLY, DashboardTagSummarySumMode.DISTRIBUTED, body);

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Son işlemler - başarılı")
  void lastTransactions_shouldReturnSuccess() {
    LastTransactionsResponseDto dto = new LastTransactionsResponseDto();
    when(dashboardService.lastTransactions(USER_ID)).thenReturn(dto);

    ApiResponse<LastTransactionsResponseDto> response = controller.lastTransactions();

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Yaklaşan taksitler - başarılı")
  void incomingInstallments_shouldReturnSuccess() {
    IncomingInstallmentsResponseDto dto = new IncomingInstallmentsResponseDto();
    when(dashboardService.incomingInstallments(USER_ID)).thenReturn(dto);

    ApiResponse<IncomingInstallmentsResponseDto> response = controller.incomingInstallments();

    assertTrue(response.isType());
  }
}
