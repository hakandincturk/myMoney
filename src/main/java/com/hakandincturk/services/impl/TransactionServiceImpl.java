package com.hakandincturk.services.impl;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.enums.sort.TransactionSortColumn;
import com.hakandincturk.core.specs.TransactionSpecifaction;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.factories.TransactionFactory;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.services.rules.TransactionRules;
import com.hakandincturk.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final InstallmentRepository installmentRepository;
  private final TransactionRules transactionRules;
  private final TransactionFactory transactionFactory;
  private final AccountFactory accountFactory;
  private final AccountRepository accountRepository;

  @Override
  @Transactional
  public void createTransaction(Long userId, CreateTransactionRequestDto body) {

    transactionRules.validateCreateTransactionRequest(body);

    User activeUser = transactionRules.getValidatedUser(userId);
    Account account = transactionRules.getValidatedAccount(userId, body.getAccountId());
    Contact contact = transactionRules.getValidatedContact(userId, body.getContactId());

    Transaction newTransaction = transactionFactory.createTransaction(body, activeUser, account, contact);    
    transactionRepository.save(newTransaction);

    if(newTransaction.getType().equals(TransactionTypes.DEBT)) {
      account = accountFactory.reCalculateBalanceOnTransactionCreate(account, newTransaction.getType(), newTransaction.getTotalAmount());
      accountRepository.save(account);
    }

  }

  @Override
  public Page<ListMyTransactionsResponseDto> listMyTransactions(Long userId, TransactionFilterRequestDto pageData) {
    Pageable pageable = PaginationUtils.toPageable(pageData, TransactionSortColumn.class);
    Specification<Transaction> specs = TransactionSpecifaction.filter(userId, pageData);
    Page<Transaction> dbTransactions = transactionRepository.findAll(specs, pageable);

    Page<ListMyTransactionsResponseDto> dtoPage = dbTransactions.map(transaction -> new ListMyTransactionsResponseDto(
      transaction.getId(),
      transaction.getName(),
      transaction.getContact() != null ? transaction.getContact().getFullName() : null,
      transaction.getAccount().getName(),
      transaction.getType().name(),
      transaction.getStatus().name(),
      transaction.getTotalAmount(),
      transaction.getPaidAmount(),
      transaction.getTotalInstallment()
    ));

    return dtoPage;
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
  
  @Override
  public List<ListInstallments> listTransactionInstallments(Long userId, Long transactionId) {
    Transaction transaction = transactionRules.checkUserTransactionExistAndGet(userId, transactionId);

    List<ListInstallments> installments = transaction.getInstallments().stream()
    .sorted(Comparator.comparing(Installment::getId))
    .map(installment -> new ListInstallments(
      installment.getId(),
      installment.getAmount(),
      installment.getDebtDate(),
      installment.getInstallmentNumber(),
      installment.getDescripton(),
      installment.isPaid(),
      installment.getPaidDate()
    )).toList();

    return installments;
  }
}
