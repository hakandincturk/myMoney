package com.hakandincturk.services.rules;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.core.exception.ValidationException;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionRules {
  
  private final TransactionRepository transactionRepository;
  private final UserRules userRules;
  private final ContactRules contactRules;
  private final AccountRules accountRules;

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

  public User getValidatedUser(Long userId){
    return userId != null ? userRules.checkUserExistAndGet(userId) : null;
  }

  public Transaction checkUserTransactionExistAndGet(Long userId, Long transactionId){
    Optional<Transaction> dbTransaction = transactionRepository.findByIdAndUserIdAndIsRemovedFalse(transactionId, userId);
    if(dbTransaction.isEmpty()){
      throw new NotFoundException("Borç bulunmadı");
    }

    return dbTransaction.get();
  }

}
