package com.hakandincturk.services.abstracts;

import org.springframework.data.domain.Page;

import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.dtos.installment.request.PayInstallmentRequestDto;
import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;

public interface InstallmentService {
  public Page<ListMySpecisifDateInstallmentsResponseDto> listMySpecisifDateInstallments(Long userId, FilterListMyInstallmentRequestDto pageData);
  public void payInstallment(Long userId, Long installmentId, PayInstallmentRequestDto body);
}
