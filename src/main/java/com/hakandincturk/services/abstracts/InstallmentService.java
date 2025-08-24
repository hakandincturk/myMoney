package com.hakandincturk.services.abstracts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;

public interface InstallmentService {
  public Page<ListMySpecisifDateInstallmentsResponseDto> listMySpecisifDateInstallments(Long userId, int month, int year, Pageable pageData);
  public void payInstallment(Long userId, Long installmentId, PayInstallmentRequestDto body);
}
