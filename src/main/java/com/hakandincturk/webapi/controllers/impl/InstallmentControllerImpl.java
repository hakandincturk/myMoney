package com.hakandincturk.webapi.controllers.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecificDateInstallmentsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.InstallmentService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.InstallmentController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/installment")
@Tag(name = "Installment", description = "Taksit işlemleri")
public class InstallmentControllerImpl extends BaseController implements InstallmentController {

  private final InstallmentService installmentService;

  @Override
  @GetMapping(value = "/month")
  @Operation(summary = "Get monthly installments", description = "Aylık taksitleri listelemeyi sağlar")
  public ApiResponse<PagedResponse<ListMySpecificDateInstallmentsResponseDto>> listMySpecisifDateInstallments(FilterListMyInstallmentRequestDto pageData) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return successPaged("Aylık borçlarınız getirildi", installmentService.listMySpecisifDateInstallments(userId, pageData));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @PatchMapping(value = "/pay")
  public ApiResponse<?> payInstallment(@RequestBody PayInstallmentRequestDto body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      installmentService.payInstallments(userId, body);
      return success("Aylık taksit ödendi");
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

}
