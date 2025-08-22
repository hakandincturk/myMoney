package com.hakandincturk.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.factories.TransactionFactory;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.services.rules.TransactionRules;

@Service
public class TransactionServiceImpl implements TransactionService {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private InstallmentRepository installmentRepository;

  @Autowired
  private TransactionRules transactionRules;

  @Autowired
  private TransactionFactory transactionFactory;

    TransactionServiceImpl(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

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

  @Override
  public List<ListMyTransactionsResponseDto> listMyTransactions(Long userId) {
    List<Transaction> dbTransactions = transactionRepository.findByUserIdAndIsRemovedFalseOrderByCreatedAtDesc(userId);

    List<ListMyTransactionsResponseDto> transactions = dbTransactions.stream().map(transaction -> new ListMyTransactionsResponseDto(
      transaction.getId(),
      transaction.getContact() != null ? transaction.getContact().getFullName() : null,
      transaction.getAccount().getName(),
      transaction.getType().name(),
      transaction.getStatus().name(),
      transaction.getTotalAmount(),
      transaction.getPaidAmount(),
      transaction.getTotalInstallment()
    )).collect(Collectors.toList());

    return transactions;
  }

  @Override
  @Transactional
  public void deleteMyTransaction(Long userId, Long transactionId) {
    Transaction transaction = transactionRules.checkUserTransactionExistAndGet(userId, transactionId);
    transaction.setRemoved(true);

    if (transaction.getInstallments() != null && !transaction.getInstallments().isEmpty()) {
      transaction.getInstallments().forEach(installment -> installment.setRemoved(true));
      installmentRepository.saveAll(transaction.getInstallments());
    }

    transactionRepository.save(transaction);
  }
  
}
