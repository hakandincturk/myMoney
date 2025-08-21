package com.hakandincturk.webapi.controllers.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.InstallmentService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.concretes.InstallmentController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/installment")
@Tag(name = "Installment", description = "Taksit işlemleri")
public class InstallmentControllerImpl extends BaseController implements InstallmentController {

  @Autowired
  private InstallmentService installmentService;

  @Override
  @GetMapping(value = "/month/{month}/{year}")
  @Operation(summary = "Get monthly installments", description = "Aylık taksitleri listelemeyi sağlar")
  public ApiResponse<List<ListMySpecisifDateInstallmentsResponseDto>> listMySpecisifDateInstallments(@PathVariable(value = "month") int month, @PathVariable(value = "year") int year) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Aylık borçlarınız getirildi", installmentService.listMySpecisifDateInstallments(userId, month, year));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
  
}
