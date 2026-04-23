package com.hakandincturk.myMoney.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.models.Account;

class AccountFactoryTest {

  private final AccountFactory accountFactory = new AccountFactory();

  @Test
  @DisplayName("Kredi kartı + DEBT: bakiye düşmeli")
  void reCalculateBalanceOnTransactionCreate_creditCard_debt_shouldSubtract() {
    Account account = new Account();
    account.setType(AccountTypes.CREDIT_CARD);
    account.setBalance(BigDecimal.valueOf(1000));

    Account result = accountFactory.reCalculateBalanceOnTransactionCreate(account, TransactionTypes.DEBT, BigDecimal.valueOf(300));

    assertEquals(BigDecimal.valueOf(700), result.getBalance());
  }

  @Test
  @DisplayName("Kredi kartı + CREDIT: bakiye değişmemeli")
  void reCalculateBalanceOnTransactionCreate_creditCard_credit_shouldNotChange() {
    Account account = new Account();
    account.setType(AccountTypes.CREDIT_CARD);
    account.setBalance(BigDecimal.valueOf(1000));

    Account result = accountFactory.reCalculateBalanceOnTransactionCreate(account, TransactionTypes.CREDIT, BigDecimal.valueOf(300));

    assertEquals(BigDecimal.valueOf(1000), result.getBalance());
  }

  @Test
  @DisplayName("Banka hesabı + DEBT: bakiye değişmemeli (sadece create)")
  void reCalculateBalanceOnTransactionCreate_bank_debt_shouldNotChange() {
    Account account = new Account();
    account.setType(AccountTypes.BANK);
    account.setBalance(BigDecimal.valueOf(5000));

    Account result = accountFactory.reCalculateBalanceOnTransactionCreate(account, TransactionTypes.DEBT, BigDecimal.valueOf(500));

    assertEquals(BigDecimal.valueOf(5000), result.getBalance());
  }

  // reCalculateBalanceOnPayment tests

  @Test
  @DisplayName("Kredi kartı DEBT ödeme: bakiye artmalı")
  void reCalculateBalanceOnPayment_creditCard_debt_shouldAdd() {
    Account account = new Account();
    account.setType(AccountTypes.CREDIT_CARD);
    account.setBalance(BigDecimal.valueOf(700));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.DEBT, BigDecimal.valueOf(100));

    assertEquals(BigDecimal.valueOf(800), result.getBalance());
  }

  @Test
  @DisplayName("Kredi kartı CREDIT ödeme: bakiye artmalı")
  void reCalculateBalanceOnPayment_creditCard_credit_shouldAdd() {
    Account account = new Account();
    account.setType(AccountTypes.CREDIT_CARD);
    account.setBalance(BigDecimal.valueOf(700));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.CREDIT, BigDecimal.valueOf(200));

    assertEquals(BigDecimal.valueOf(900), result.getBalance());
  }

  @Test
  @DisplayName("Kredi kartı PAYMENT: bakiye düşmeli")
  void reCalculateBalanceOnPayment_creditCard_payment_shouldSubtract() {
    Account account = new Account();
    account.setType(AccountTypes.CREDIT_CARD);
    account.setBalance(BigDecimal.valueOf(1000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.PAYMENT, BigDecimal.valueOf(300));

    assertEquals(BigDecimal.valueOf(700), result.getBalance());
  }

  @Test
  @DisplayName("Kredi kartı COLLECTION: bakiye düşmeli")
  void reCalculateBalanceOnPayment_creditCard_collection_shouldSubtract() {
    Account account = new Account();
    account.setType(AccountTypes.CREDIT_CARD);
    account.setBalance(BigDecimal.valueOf(1000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.COLLECTION, BigDecimal.valueOf(200));

    assertEquals(BigDecimal.valueOf(800), result.getBalance());
  }

  @Test
  @DisplayName("Banka DEBT ödeme: bakiye düşmeli")
  void reCalculateBalanceOnPayment_bank_debt_shouldSubtract() {
    Account account = new Account();
    account.setType(AccountTypes.BANK);
    account.setBalance(BigDecimal.valueOf(5000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.DEBT, BigDecimal.valueOf(1000));

    assertEquals(BigDecimal.valueOf(4000), result.getBalance());
  }

  @Test
  @DisplayName("Banka PAYMENT ödeme: bakiye düşmeli")
  void reCalculateBalanceOnPayment_bank_payment_shouldSubtract() {
    Account account = new Account();
    account.setType(AccountTypes.BANK);
    account.setBalance(BigDecimal.valueOf(5000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.PAYMENT, BigDecimal.valueOf(500));

    assertEquals(BigDecimal.valueOf(4500), result.getBalance());
  }

  @Test
  @DisplayName("Banka CREDIT ödeme: bakiye artmalı")
  void reCalculateBalanceOnPayment_bank_credit_shouldAdd() {
    Account account = new Account();
    account.setType(AccountTypes.BANK);
    account.setBalance(BigDecimal.valueOf(5000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.CREDIT, BigDecimal.valueOf(2000));

    assertEquals(BigDecimal.valueOf(7000), result.getBalance());
  }

  @Test
  @DisplayName("Banka COLLECTION ödeme: bakiye artmalı")
  void reCalculateBalanceOnPayment_bank_collection_shouldAdd() {
    Account account = new Account();
    account.setType(AccountTypes.BANK);
    account.setBalance(BigDecimal.valueOf(5000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.COLLECTION, BigDecimal.valueOf(1500));

    assertEquals(BigDecimal.valueOf(6500), result.getBalance());
  }

  @Test
  @DisplayName("Nakit DEBT ödeme: bakiye düşmeli")
  void reCalculateBalanceOnPayment_cash_debt_shouldSubtract() {
    Account account = new Account();
    account.setType(AccountTypes.CASH);
    account.setBalance(BigDecimal.valueOf(3000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.DEBT, BigDecimal.valueOf(500));

    assertEquals(BigDecimal.valueOf(2500), result.getBalance());
  }

  @Test
  @DisplayName("Nakit COLLECTION ödeme: bakiye artmalı")
  void reCalculateBalanceOnPayment_cash_collection_shouldAdd() {
    Account account = new Account();
    account.setType(AccountTypes.CASH);
    account.setBalance(BigDecimal.valueOf(3000));

    Account result = accountFactory.reCalculateBalanceOnPayment(account, TransactionTypes.COLLECTION, BigDecimal.valueOf(800));

    assertEquals(BigDecimal.valueOf(3800), result.getBalance());
  }
}
