package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecificDateInstallmentsResponseDto;

public interface InstallmentController {
  ApiResponse<PagedResponse<ListMySpecificDateInstallmentsResponseDto>> listMySpecisifDateInstallments(FilterListMyInstallmentRequestDto pageData);
  ApiResponse<?> payInstallment(Long installmentId, PayInstallmentRequestDto body);
}
