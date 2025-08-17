package com.hakandincturk.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.dtos.installment.response.ListMySpecisifDateInstallmentsResponseDto;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.services.abstracts.InstallmentService;
import com.hakandincturk.services.rules.InstallmentRules;

@Service
public class InstallmentServiceImpl implements InstallmentService {

  @Autowired
  private InstallmentRepository installmentRepository;

  private InstallmentRules installmentRules;

  @Override
  public List<ListMySpecisifDateInstallmentsResponseDto> listMySpecisifDateInstallments(Long userId, int month, int year) {
    User user = installmentRules.getValidatedUser(userId);
    List<Installment> dbInstallments = installmentRepository.findByTransactionUserIdAndDebtDateBetween(userId, null, null);

    // dbInstallments.stream().map(installment -> {
    //   System.out.println(installment);
    // });

    dbInstallments.forEach(installment -> {
      System.out.println("----------");
      System.out.println("Installment number" + installment.getInstallmentNumber());
      System.out.println("amount" + installment.getAmount());
      System.out.println("getDebtDate" + installment.getDebtDate());
    });

    return null;
  }
  
}
