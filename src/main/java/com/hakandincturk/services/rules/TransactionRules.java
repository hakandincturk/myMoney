package com.hakandincturk.services.rules;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.ValidationException;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;

@Service
public class TransactionRules {

  @Autowired
  private ContactRules contactRules;

  @Autowired
  private AccountRules accountRules;
  
  public void validateCreateTransactionRequest(CreateTransactionRequestDto body){
    if(body.getAccountId() == null) {
      throw new ValidationException("Hesap boş olamaz");
    }
    if(body.getTotalAmount() == null || body.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0){
      throw new ValidationException("Toplam miktar 0'dan büyük olmak zorunda");
    }
    if(body.getTotalInstallment() < 0){
      throw new ValidationException("Taksit miktaro 0'dan küçük olamaz");
    }
  }

  public Contact getValidatedContact(Long userId, Long contactId){
    return contactId != null ? contactRules.checkUserContactExistAndGet(userId, contactId) : null;
  }

  public Account getValidatedAccount(Long userId, Long accountId){
    return accountId != null ? accountRules.checkUserAccountExistAndGet(userId, accountId) : null;
  }

}
