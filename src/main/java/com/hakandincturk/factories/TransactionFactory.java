package com.hakandincturk.factories;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Category;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionCategory;
import com.hakandincturk.models.User;

@Component
public class TransactionFactory {

  private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;

  public Transaction createTransaction(CreateTransactionRequestDto body, User user, Account account, Contact contact, List<Category> categories){
    Transaction newTransaction = new Transaction();
    newTransaction.setAccount(account);
    newTransaction.setContact(contact);
    newTransaction.setType(body.getType());
    newTransaction.setStatus(TransactionStatuses.PENDING);
    newTransaction.setDescription(body.getDescription());
    newTransaction.setTotalInstallment(body.getTotalInstallment());
    newTransaction.setPaidAmount(ZERO_AMOUNT);
    newTransaction.setDebtDate(body.getDebtDate());
    newTransaction.setUser(user);
    newTransaction.setName(body.getName());

    BigDecimal totalAmount = !body.isEqualSharingBetweenInstallments() ? body.getTotalAmount().multiply(BigDecimal.valueOf(body.getTotalInstallment())) : body.getTotalAmount();
    newTransaction.setTotalAmount(totalAmount);

    if(body.getTotalInstallment() > 0) {
      List<Installment> installments = generateInstallments(newTransaction, body);
      newTransaction.setInstallments(installments);
    }

    if(categories.size() > 0){
      List<TransactionCategory> newCategories = generateTransactionCategories(newTransaction, categories);
      newTransaction.setTransactionCategories(newCategories);
    }

    return newTransaction;
  }

  private List<Installment> generateInstallments(Transaction transaction, CreateTransactionRequestDto body){
    BigDecimal installmentAmount = !body.isEqualSharingBetweenInstallments() ? body.getTotalAmount() : body.getTotalAmount().divide(BigDecimal.valueOf(body.getTotalInstallment()), 2, RoundingMode.HALF_UP);

    List<Installment> installments = new ArrayList<Installment>();
    LocalDate baseDate = body.getDebtDate();
    for (int i = 1; i <= body.getTotalInstallment(); i++) {
      LocalDate installmentDate = baseDate.plusMonths(i - 1);
      installments.add(new Installment(
        transaction,
        i,
        installmentAmount,
        false,
        null,
        null,
        installmentDate
      ));
    }
    
    return installments;
  }

  private List<TransactionCategory> generateTransactionCategories(Transaction transaction, List<Category> categories){
    List<TransactionCategory> transactionCategories = new ArrayList<TransactionCategory>();
    for (Category category : categories) {
      transactionCategories.add(new TransactionCategory(transaction, category));
    }

    return transactionCategories;
  }
}
