package com.hakandincturk.services.rules;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.exception.BusinessException;
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

  public Installment checkUserSingleInstallmentExistAndGet(Long userId, Long installmentId){
    return installmentRepository.findByIdAndTransactionUserIdAndIsRemovedFalse(installmentId, userId)
      .orElseThrow(() -> new NotFoundException("Taksit bulunamadı"));
  }

  public void checkInstallmentCanBeSkipped(Installment installment){
    if(installment.isPaid()){
      throw new BusinessException("Ödenmiş bir taksit ödenmeyecek olarak işaretlenemez");
    }
  }

  public void checkInstallmentCanBeUpdated(Installment installment, InstallmentStatuses newStatus){
    if(installment.isPaid() && newStatus == InstallmentStatuses.SKIPPED){
      throw new BusinessException("Ödenmiş bir taksit ödenmeyecek olarak işaretlenemez");
    }
    if(installment.getStatus() == InstallmentStatuses.SKIPPED && newStatus == null){
      throw new BusinessException("Ödenmeyecek durumdaki bir taksidin tutarı değiştirilemez, önce aktif yapınız");
    }
  }

  public Users getValidatedUser(Long userId){
    return userId != null ? userRules.checkUserExistAndGet(userId) : null;
  }

}
