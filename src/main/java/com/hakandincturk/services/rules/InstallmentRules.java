package com.hakandincturk.services.rules;

import java.util.List;

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

  public List<Installment> checkUserInstallmentExistAndGet(Long userId, List<Long> installmentIds){
    List<Installment> dbInstallment = installmentRepository.findByIdInAndTransactionUserIdAndIsRemovedFalse(installmentIds, userId);
    if(dbInstallment.size() != installmentIds.size()){
      throw new NotFoundException("Taksitler bulunamadı");
    }

    return dbInstallment;
  }


  public Users getValidatedUser(Long userId){
    return userId != null ? userRules.checkUserExistAndGet(userId) : null;
  }
  
}
