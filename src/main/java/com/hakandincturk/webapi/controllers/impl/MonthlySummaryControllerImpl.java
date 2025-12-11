package com.hakandincturk.webapi.controllers.impl;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.monthlySummery.request.BackFillMonthlySummeriesRequest;
import com.hakandincturk.services.abstracts.MonthlySummaryService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.MonthlySummaryController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/monthly-summery")
public class MonthlySummaryControllerImpl extends BaseController implements MonthlySummaryController {

  private final MonthlySummaryService monthlySummaryService;
  
  @Override
  @PostMapping(value = "/backfill/specific-users")
  @Operation(summary = "Calculate monthly summeries for specific users", description = "Secilen kullanicilar icin aylik ozet hesaplamalarini yapar")
  public ApiResponse<?> fillForUsers(@Valid @RequestBody BackFillMonthlySummeriesRequest body) {
    monthlySummaryService.fillForUsers(body);
    return success("Seçilen kullanıcaların hesaplamaları yapıldı", null);
  }

  @Override
  @PostMapping(value = "/backfill/all-users")
  @Operation(summary = "Calculate monthly summeries for all users", description = "Butun kullanicilar icin aylik ozet hesaplamalarini yapar")
  public ApiResponse<?> fillForAllUsers() {
    monthlySummaryService.fillForAllUsers();
    return success("Kullanıcaların hesaplamaları yapıldı", null);
  }

}
