package com.hakandincturk.services.rules;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.InstallmentRepository;

@Service
public class InstallmentRules {

  @Autowired
  private InstallmentRepository installmentRepository;

  @Autowired
  private UserRules userRules;

  public Installment checkUserInstallmentExistAndGet(Long userId, Long installmentId){
  Optional<Installment> dbInstallment = installmentRepository.findByIdAndTransactionUserIdAndIsRemovedFalse(installmentId, userId);
    if(dbInstallment.isEmpty()){
      throw new NotFoundException("Taksit bulunamadı");
    }

    return dbInstallment.get();
  }


  public User getValidatedUser(Long userId){
    return userId != null ? userRules.checkUserExistAndGet(userId) : null;
  }
  
}
