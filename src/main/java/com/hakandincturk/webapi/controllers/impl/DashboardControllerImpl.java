package com.hakandincturk.webapi.controllers.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.dashboard.response.MonthlyTrend;
import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.DashboardService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.DashboardController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/dashboard")
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
      return success("Ana sayfa verileri başarılı bir şekilde getirildi", dashboardService.quickViewResponse(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
  
  @Override
  @GetMapping(value = "/monthly-trend")
  @Operation(summary = "Monthly Trend", description = "Ana sayfa gelir/gider trendi grafigi verilerini getirir")
  public ApiResponse<MonthlyTrend> monthlyTrend() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Ana sayfa verileri başarılı bir şekilde getirildi", dashboardService.monthlyTrend(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
  
}
