package com.hakandincturk.factories;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.models.Account;

@Component
public class AccountFactory {

  public Account reCalculateBalanceOnTransactionCreate(Account account, TransactionTypes transactionType, BigDecimal amount){
    if(account.getType().equals(AccountTypes.CREDIT_CARD) && transactionType.equals(TransactionTypes.DEBT)){
      BigDecimal newBalance = account.getBalance().subtract(amount);
      account.setBalance(newBalance);
    }
    return account;
  }

  public Account reCalculateBalanceOnPayment(Account account, TransactionTypes transactionType, BigDecimal amount){
    BigDecimal currentBalance = account.getBalance();

    switch(account.getType()){
      case CREDIT_CARD:{
        switch(transactionType){
          case DEBT:
          case CREDIT: {   
            currentBalance = currentBalance.add(amount);
            break;
          }
          case COLLECTION: 
          case PAYMENT: {
            currentBalance = currentBalance.subtract(amount);
            break;
          }
        }
        break;
      }
      case CASH:
      case BANK:{
        switch(transactionType){
          case DEBT:
          case PAYMENT:{
            currentBalance = currentBalance.subtract(amount);
            break;
          }
          case CREDIT:
          case COLLECTION:{
            currentBalance = currentBalance.add(amount);
            break;
          }
        }
        break;
      }
    }
    
    account.setBalance(currentBalance);
    return account;
  }
  
}
