package com.hakandincturk.webapi.controllers.concretes;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;

public interface InstallmentController {
  public ApiResponse<PagedResponse<ListMySpecisifDateInstallmentsResponseDto>> listMySpecisifDateInstallments(FilterListMyInstallmentRequestDto pageData);
  public ApiResponse<?> payInstallment(Long installmentId, PayInstallmentRequestDto body);
}
