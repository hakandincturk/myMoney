package com.hakandincturk.webapi.controllers.concretes;

import java.util.List;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;

public interface InstallmentController {
  public ApiResponse<List<ListMySpecisifDateInstallmentsResponseDto>> listMySpecisifDateInstallments(int month, int year);
  public ApiResponse<?> payInstallment(Long installmentId, PayInstallmentRequestDto body);
}
