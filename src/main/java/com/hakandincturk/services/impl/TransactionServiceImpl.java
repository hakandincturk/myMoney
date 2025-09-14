package com.hakandincturk.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.enums.sort.TransactionSortColumn;
import com.hakandincturk.core.specs.TransactionSpecification;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.factories.CategoryFactory;
import com.hakandincturk.factories.TransactionFactory;
import com.hakandincturk.mappers.TransactionMapper;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Category;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.CategoryRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.services.rules.CategoryRules;
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
  private final TransactionMapper transactionMapper;
  private final CategoryFactory categoryFactory;
  private final CategoryRules categoryRules;
  private final CategoryRepository categoryRepository;

  @Override
  @Transactional
  public void createTransaction(Long userId, CreateTransactionRequestDto body) {

    transactionRules.validateCreateTransactionRequest(body);

    User activeUser = transactionRules.getValidatedUser(userId);
    Account account = transactionRules.getValidatedAccount(userId, body.getAccountId());
    Contact contact = transactionRules.getValidatedContact(userId, body.getContactId());
    
    List<Category> categories = new ArrayList<>();
    if(body.getCategory().getCategoryIds().size() > 0){
      List<Category> dbCategories = categoryRules.checkAllIdsAndGet(body.getCategory().getCategoryIds());
      categories.addAll(dbCategories);
    }

    List<Category> newCategories = body.getCategory().getNewCategories().stream().map(categoryName -> categoryFactory.createCategory(categoryName, activeUser)).toList();
    categoryRepository.saveAll(newCategories);
    categories.addAll(newCategories);
    

    Transaction newTransaction = transactionFactory.createTransaction(body, activeUser, account, contact, categories);    
    transactionRepository.save(newTransaction);

    if(newTransaction.getType().equals(TransactionTypes.DEBT)) {
      account = accountFactory.reCalculateBalanceOnTransactionCreate(account, newTransaction.getType(), newTransaction.getTotalAmount());
      accountRepository.save(account);
    }

  }

  @Override
  public Page<ListMyTransactionsResponseDto> listMyTransactions(Long userId, TransactionFilterRequestDto pageData) {
    Pageable pageable = PaginationUtils.toPageable(pageData, TransactionSortColumn.class);
    Specification<Transaction> specs = TransactionSpecification.filter(userId, pageData);
    Page<Transaction> dbTransactions = transactionRepository.findAll(specs, pageable);
    return dbTransactions.map(transactionMapper::toListMyTransactionsResponseDto);
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
