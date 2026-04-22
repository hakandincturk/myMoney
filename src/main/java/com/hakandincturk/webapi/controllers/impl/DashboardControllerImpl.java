package com.hakandincturk.webapi.controllers.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.enums.DashboardTagSummarySumMode;
import com.hakandincturk.core.enums.DashboardTagSummaryTypes;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.dashboard.request.TagSummaryRequestDto;
import com.hakandincturk.dtos.dashboard.response.TagSummaryResponseDto;
import com.hakandincturk.dtos.dashboard.response.IncomingInstallmentsResponseDto;
import com.hakandincturk.dtos.dashboard.response.LastTransactionsResponseDto;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrendResponseDto;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.DashboardService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.DashboardController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Ana sayfa işlemleri")
public class DashboardControllerImpl extends BaseController implements DashboardController {

  private final DashboardService dashboardService;

  @Override
  @GetMapping(value = "/quick-view")
  @Operation(summary = "Quick View", description = "Ana sayfa hızlı bakış verilerini getirir")
  public ApiResponse<QuickViewResponseDto> quickViewResponse() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Ana sayfa hızlı bakış verilerini getirir", dashboardService.quickViewResponse(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/monthly-trend")
  @Operation(summary = "Monthly Trend", description = "Ana sayfa gelir/gider trendi grafigi verilerini getirir")
  public ApiResponse<MonthlyTrendResponseDto> monthlyTrend() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Ana sayfa gelir/gider trendi grafigi verilerini getirir", dashboardService.monthlyTrend(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @PostMapping(value = "/tag-summary")
  @Operation(summary = "Tag Summary", description = "Ana sayfada harcamalari etiket bazında getirir")
  public ApiResponse<TagSummaryResponseDto> tagSummary(@RequestParam(value = "type") DashboardTagSummaryTypes type, DashboardTagSummarySumMode sumMode, @Valid @RequestBody TagSummaryRequestDto body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Ana sayfa etiket bazlı harcamalar getirildi", dashboardService.tagSummary(userId, type, sumMode, body));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/last-transactions")
  @Operation(summary = "Last 10 Transactions", description = "En son kaydedilen gelir ve gider hareketleri")
  public ApiResponse<LastTransactionsResponseDto> lastTransactions() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("En son kaydedilen gelir ve gider hareketleri getirildi", dashboardService.lastTransactions(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/incoming-transactions")
  @Operation(summary = "Get the nearest installments", description = "Yaklaşan ödemeler getirildi")
  public ApiResponse<IncomingInstallmentsResponseDto> incomingInstallments() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Yaklaşan ödemeler getirildi", dashboardService.incomingInstallments(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

}
