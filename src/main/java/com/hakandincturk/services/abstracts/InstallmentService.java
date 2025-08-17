package com.hakandincturk.services.abstracts;

import java.util.List;

import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;

public interface InstallmentService {
  public List<ListMySpecisifDateInstallmentsResponseDto> listMySpecisifDateInstallments(Long userId, int month, int year);
}
