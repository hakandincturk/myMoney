package com.hakandincturk.myMoney.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.CreateTransactionTagDetail;
import com.hakandincturk.factories.TransactionFactory;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;

class TransactionFactoryTest {

  private final TransactionFactory transactionFactory = new TransactionFactory();

  @Test
  @DisplayName("Eşit taksit paylaşımı ile transaction oluşturulmalı")
  void createTransaction_withEqualSharing_shouldCreateCorrectly() {
    Users user = new Users();
    user.setId(1L);
    Account account = new Account();
    account.setId(10L);

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.DEBT);
    body.setTotalAmount(BigDecimal.valueOf(3000));
    body.setTotalInstallment(3);
    body.setDescription("Test borç");
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setName("Test Transaction");
    body.setEqualSharingBetweenInstallments(true);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    Transaction result = transactionFactory.createTransaction(body, user, account, null, List.of());

    assertNotNull(result);
    assertEquals(TransactionTypes.DEBT, result.getType());
    assertEquals(TransactionStatuses.PENDING, result.getStatus());
    assertEquals(BigDecimal.valueOf(3000), result.getTotalAmount());
    assertEquals(BigDecimal.ZERO, result.getPaidAmount());
    assertEquals(3, result.getTotalInstallment());
    assertEquals(user, result.getUser());
    assertEquals(account, result.getAccount());
    assertNull(result.getContact());
    assertEquals(3, result.getInstallments().size());
  }

  @Test
  @DisplayName("Eşit olmayan taksit paylaşımı ile total amount hesaplanmalı")
  void createTransaction_withNonEqualSharing_shouldMultiplyAmount() {
    Users user = new Users();
    user.setId(1L);
    Account account = new Account();
    account.setId(10L);

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.PAYMENT);
    body.setTotalAmount(BigDecimal.valueOf(500));
    body.setTotalInstallment(4);
    body.setDescription("Aylık ödeme");
    body.setDebtDate(LocalDate.of(2025, 1, 1));
    body.setName("Aylık Ödeme");
    body.setEqualSharingBetweenInstallments(false);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    Transaction result = transactionFactory.createTransaction(body, user, account, null, List.of());

    assertEquals(BigDecimal.valueOf(2000), result.getTotalAmount());
    assertEquals(4, result.getInstallments().size());

    result.getInstallments().forEach(installment ->
        assertEquals(0, BigDecimal.valueOf(500).compareTo(installment.getAmount()))
    );
  }

  @Test
  @DisplayName("Tag'ler ile transaction oluşturulmalı")
  void createTransaction_withTags_shouldCreateTransactionTags() {
    Users user = new Users();
    user.setId(1L);
    Account account = new Account();
    account.setId(10L);

    Tag tag1 = new Tag();
    tag1.setId(1L);
    tag1.setName("Yemek");
    Tag tag2 = new Tag();
    tag2.setId(2L);
    tag2.setName("Ulaşım");

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.DEBT);
    body.setTotalAmount(BigDecimal.valueOf(1000));
    body.setTotalInstallment(1);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setName("Test");
    body.setEqualSharingBetweenInstallments(true);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    Transaction result = transactionFactory.createTransaction(body, user, account, null, List.of(tag1, tag2));

    assertNotNull(result.getTransactionTags());
    assertEquals(2, result.getTransactionTags().size());
  }

  @Test
  @DisplayName("Contact ile transaction oluşturulmalı")
  void createTransaction_withContact_shouldSetContact() {
    Users user = new Users();
    user.setId(1L);
    Account account = new Account();
    account.setId(10L);
    Contact contact = new Contact();
    contact.setId(5L);

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.CREDIT);
    body.setTotalAmount(BigDecimal.valueOf(2000));
    body.setTotalInstallment(2);
    body.setDebtDate(LocalDate.of(2025, 3, 1));
    body.setName("Alacak");
    body.setEqualSharingBetweenInstallments(true);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    Transaction result = transactionFactory.createTransaction(body, user, account, contact, List.of());

    assertEquals(contact, result.getContact());
  }

  @Test
  @DisplayName("Taksit sayısı 0 olduğunda taksit listesi oluşturulmamalı")
  void createTransaction_withZeroInstallments_shouldNotGenerateInstallments() {
    Users user = new Users();
    user.setId(1L);
    Account account = new Account();
    account.setId(10L);

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.DEBT);
    body.setTotalAmount(BigDecimal.valueOf(500));
    body.setTotalInstallment(0);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setName("Taksitsiz");
    body.setEqualSharingBetweenInstallments(true);
    body.setTag(new CreateTransactionTagDetail(List.of(), List.of()));

    Transaction result = transactionFactory.createTransaction(body, user, account, null, List.of());

    assertNull(result.getInstallments());
  }

  @Test
  @DisplayName("Taksitler doğru tarihlerle oluşturulmalı")
  void generateInstallments_shouldCreateCorrectDates() {
    Transaction transaction = new Transaction();
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setTotalAmount(BigDecimal.valueOf(1200));
    body.setTotalInstallment(3);
    body.setDebtDate(LocalDate.of(2025, 1, 15));
    body.setEqualSharingBetweenInstallments(true);

    List<Installment> result = transactionFactory.generateInstallments(transaction, body);

    assertEquals(3, result.size());
    assertEquals(LocalDate.of(2025, 1, 15), result.get(0).getDebtDate());
    assertEquals(LocalDate.of(2025, 2, 15), result.get(1).getDebtDate());
    assertEquals(LocalDate.of(2025, 3, 15), result.get(2).getDebtDate());
    assertEquals(1, result.get(0).getInstallmentNumber());
    assertEquals(2, result.get(1).getInstallmentNumber());
    assertEquals(3, result.get(2).getInstallmentNumber());
    assertFalse(result.get(0).isPaid());
  }
}
