package com.hakandincturk.webapi.controllers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.enums.sort.InstallmentSortColumn;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.InstallmentService;
import com.hakandincturk.utils.PaginationUtils;
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
  public ApiResponse<PagedResponse<ListMySpecisifDateInstallmentsResponseDto>> listMySpecisifDateInstallments(@PathVariable(value = "month") int month, @PathVariable(value = "year") int year, @ModelAttribute SortablePageRequest pageData) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      Pageable pageable = PaginationUtils.toPageable(pageData, InstallmentSortColumn.class);
      return successPaged("Aylık borçlarınız getirildi", installmentService.listMySpecisifDateInstallments(userId, month, year, pageable));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @PatchMapping(value = "/pay/{installmentId}")
  public ApiResponse<?> payInstallment(@PathVariable(value = "installmentId") Long installmentId, @RequestBody PayInstallmentRequestDto body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      installmentService.payInstallment(userId, installmentId, body);
      return success("Aylık taksit ödendi");
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

}
