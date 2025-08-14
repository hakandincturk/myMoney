package com.hakandincturk.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.services.rules.AccountRules;
import com.hakandincturk.services.rules.ContactRules;
import com.hakandincturk.services.rules.TransactionRules;

@Service
public class TransactionServiceImpl implements TransactionService {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private TransactionRules transactionRules;

  @Autowired
  private ContactRules contactRules;

  @Autowired
  private AccountRules accountRules;

  @Override
  @Transactional
  public void createTransaction(Long userId, CreateTransactionRequestDto body) {
    Transaction newTransaction = new Transaction();
    newTransaction.setTotalAmomunt(body.getTotalAmount());
    newTransaction.setType(body.getType());
    newTransaction.setStatus(TransactionStatuses.PENDING);
    newTransaction.setPaidAmount(BigDecimal.valueOf(0));

    if(body.getDescription() != null) {
      newTransaction.setDescription(body.getDescription());
    }

    if(body.getContactId() != null){
      Contact dbUserContact = contactRules.checkUserContactExistAndGet(userId, body.getContactId());
      newTransaction.setContact(dbUserContact);
    }

    Account dbUserAccount = accountRules.checkUserAccountExistAndGet(userId, body.getAccountId());
    newTransaction.setAccount(dbUserAccount);

    if(body.getTotalInstallment() != null && body.getTotalInstallment() > 0) {
      BigDecimal installmentAmount = body.getTotalAmount().divide(BigDecimal.valueOf(body.getTotalInstallment()));

      List<Installment> installments = new ArrayList<Installment>();
      for (int i = 1; i <= body.getTotalInstallment(); i++) {
        installments.add(new Installment(newTransaction, i, installmentAmount, false, null));
      }

      newTransaction.setInstallments(installments);
    }

    transactionRepository.save(newTransaction);
  }
  
}
