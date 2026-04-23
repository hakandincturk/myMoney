package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecificDateInstallmentsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.InstallmentService;
import com.hakandincturk.webapi.controllers.impl.InstallmentControllerImpl;

@ExtendWith(MockitoExtension.class)
class InstallmentControllerTest {

  @InjectMocks
  private InstallmentControllerImpl controller;

  @Mock
  private InstallmentService installmentService;

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
  @DisplayName("Aylık taksit listeleme - başarılı")
  void listMySpecisifDateInstallments_shouldReturnSuccess() {
    Page<ListMySpecificDateInstallmentsResponseDto> page = new PageImpl<>(List.of());
    when(installmentService.listMySpecisifDateInstallments(eq(USER_ID), any())).thenReturn(page);

    FilterListMyInstallmentRequestDto pageData = new FilterListMyInstallmentRequestDto();
    pageData.setMonth(6);
    pageData.setYear(2025);

    ApiResponse<PagedResponse<ListMySpecificDateInstallmentsResponseDto>> response = controller.listMySpecisifDateInstallments(pageData);

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Aylık taksit listeleme - auth başarısız")
  void listMySpecisifDateInstallments_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass"));

    FilterListMyInstallmentRequestDto pageData = new FilterListMyInstallmentRequestDto();
    ApiResponse<PagedResponse<ListMySpecificDateInstallmentsResponseDto>> response = controller.listMySpecisifDateInstallments(pageData);

    assertFalse(response.isType());
  }

  @Test
  @DisplayName("Taksit ödeme - başarılı")
  void payInstallment_shouldReturnSuccess() {
    PayInstallmentRequestDto body = new PayInstallmentRequestDto(List.of(1L, 2L), LocalDate.now());

    ApiResponse<?> response = controller.payInstallment(body);

    assertTrue(response.isType());
    verify(installmentService).payInstallments(USER_ID, body);
  }
}
