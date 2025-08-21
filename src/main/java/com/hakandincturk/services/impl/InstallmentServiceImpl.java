package com.hakandincturk.services.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;
import com.hakandincturk.dtos.installment.response.TransactionDetailDto;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.services.abstracts.InstallmentService;
import com.hakandincturk.services.rules.InstallmentRules;

@Service
public class InstallmentServiceImpl implements InstallmentService {

  @Autowired
  private InstallmentRepository installmentRepository;

  @Autowired
  private InstallmentRules installmentRules;

  @Override
  public List<ListMySpecisifDateInstallmentsResponseDto> listMySpecisifDateInstallments(Long userId, int month, int year) {
    // User user = installmentRules.getValidatedUser(userId);

    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
    List<Installment> dbInstallments = installmentRepository.findByTransactionUserIdAndDebtDateBetween(userId, startDate, endDate);

    // dbInstallments.stream().map(installment -> {
    //   System.out.println(installment);
    // });

    List<ListMySpecisifDateInstallmentsResponseDto> installments = dbInstallments.stream().map(installment -> {
      TransactionDetailDto transactionDetail = new TransactionDetailDto(
        installment.getTransaction().getId(),
        installment.getTransaction().getName()
      );

      return new ListMySpecisifDateInstallmentsResponseDto(
        transactionDetail,
        installment.getAmount(),
        installment.getDebtDate(),
        installment.getInstallmentNumber(),
        installment.getDescripton(),
        installment.isPaid(),
        installment.getPaidDate()
      );
    }).toList();

    return installments;
  }
  
}
