package com.hakandincturk.factories;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;

@Component
public class TransactionFactory {

  private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;

  public Transaction createTransaction(CreateTransactionRequestDto body, Account account, Contact contact){

    Transaction newTransaction = new Transaction();
    newTransaction.setAccount(account);
    newTransaction.setContact(contact);
    newTransaction.setType(body.getType());
    newTransaction.setStatus(TransactionStatuses.PENDING);
    newTransaction.setDescription(body.getDescription());
    newTransaction.setTotalAmount(body.getTotalAmount());
    newTransaction.setPaidAmount(ZERO_AMOUNT);

    if(body.getTotalInstallment() > 0) {
      List<Installment> installments = generateInstallments(newTransaction, body);
      newTransaction.setInstallments(installments);
    }

    return newTransaction;
  }

  private List<Installment> generateInstallments(Transaction transaction, CreateTransactionRequestDto body){
    BigDecimal installmentAmount = body.getTotalAmount().divide(BigDecimal.valueOf(body.getTotalInstallment()), 2, RoundingMode.HALF_UP);

    List<Installment> installments = new ArrayList<Installment>();
    for (int i = 1; i <= body.getTotalInstallment(); i++) {
      installments.add(new Installment(transaction, i, installmentAmount, false, null, null));
    }
    
    return installments;
  }


  
}
