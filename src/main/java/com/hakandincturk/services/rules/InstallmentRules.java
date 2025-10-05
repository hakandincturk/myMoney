package com.hakandincturk.services.rules;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.InstallmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstallmentRules {

  private final InstallmentRepository installmentRepository;

  private final UserRules userRules;

  public Installment checkUserInstallmentExistAndGet(Long userId, Long installmentId){
  Optional<Installment> dbInstallment = installmentRepository.findByIdAndTransactionUserIdAndIsRemovedFalse(installmentId, userId);
    if(dbInstallment.isEmpty()){
      throw new NotFoundException("Taksit bulunamadı");
    }

    return dbInstallment.get();
  }


  public Users getValidatedUser(Long userId){
    return userId != null ? userRules.checkUserExistAndGet(userId) : null;
  }
  
}
