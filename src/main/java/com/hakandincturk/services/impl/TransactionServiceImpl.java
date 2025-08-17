package com.hakandincturk.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.factories.TransactionFactory;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.services.rules.TransactionRules;

@Service
public class TransactionServiceImpl implements TransactionService {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private TransactionRules transactionRules;

  @Autowired
  private TransactionFactory transactionFactory;

  @Override
  @Transactional
  public void createTransaction(Long userId, CreateTransactionRequestDto body) {

    transactionRules.validateCreateTransactionRequest(body);

    User activeUser = transactionRules.getValidatedUser(userId);
    Account account = transactionRules.getValidatedAccount(userId, body.getAccountId());
    Contact contact = transactionRules.getValidatedContact(userId, body.getContactId());

    Transaction newTransaction = transactionFactory.createTransaction(body, activeUser, account, contact);    
    transactionRepository.save(newTransaction);
  }
  
}
