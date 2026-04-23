package com.hakandincturk.myMoney.integration.spec;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.specs.TransactionSpecification;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.Users;

class TransactionSpecificationTest extends BaseSpecificationTest {

  @Test
  @DisplayName("İsim ile filtreleme")
  void shouldFilterByName() {
    createTransaction("Market Alışverişi", TransactionTypes.DEBT);
    createTransaction("Maaş", TransactionTypes.CREDIT);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setName("Market");

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Tipe göre filtreleme")
  void shouldFilterByType() {
    createTransaction("Borç 1", TransactionTypes.DEBT);
    createTransaction("Borç 2", TransactionTypes.DEBT);
    createTransaction("Alacak", TransactionTypes.CREDIT);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setTypes(List.of(TransactionTypes.DEBT));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(2, result.getTotalElements());
  }

  @Test
  @DisplayName("Tutar aralığına göre filtreleme")
  void shouldFilterByAmountRange() {
    createTransactionWithAmount("Küçük", BigDecimal.valueOf(100));
    createTransactionWithAmount("Orta", BigDecimal.valueOf(500));
    createTransactionWithAmount("Büyük", BigDecimal.valueOf(2000));

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setMinAmount(BigDecimal.valueOf(200));
    filter.setMaxAmount(BigDecimal.valueOf(1000));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Orta", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Statüye göre filtreleme")
  void shouldFilterByStatus() {
    createTransaction("Bekleyen", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("Ödenen", TransactionTypes.DEBT);
    t2.setStatus(TransactionStatuses.PAID);
    transactionRepository.save(t2);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setStatuses(List.of(TransactionStatuses.PENDING));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Tarih aralığına göre filtreleme")
  void shouldFilterByDateRange() {
    Transaction t1 = createTransaction("Haziran", TransactionTypes.DEBT);
    t1.setDebtDate(LocalDate.of(2025, 6, 15));
    transactionRepository.save(t1);
    Transaction t2 = createTransaction("Temmuz", TransactionTypes.DEBT);
    t2.setDebtDate(LocalDate.of(2025, 7, 15));
    transactionRepository.save(t2);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setStartDate(LocalDate.of(2025, 6, 1));
    filter.setEndDate(LocalDate.of(2025, 6, 30));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Haziran", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Başka kullanıcının verilerini getirmemeli")
  void shouldNotReturnOtherUsersData() {
    Users otherUser = new Users();
    otherUser.setFullName("Other");
    otherUser.setEmail("other@test.com");
    otherUser.setPassword("pass");
    otherUser.setPhone("111");
    otherUser = userRepository.save(otherUser);
    createTransaction("Benim İşlemim", TransactionTypes.DEBT);

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(otherUser.getId(), new TransactionFilterRequestDto()), PageRequest.of(0, 10));
    assertEquals(0, result.getTotalElements());
  }

  @Test
  @DisplayName("Hesap ID'ye göre filtreleme")
  void shouldFilterByAccountIds() {
    Account account2 = createSecondAccount("Diğer Hesap");
    createTransaction("Hesap1", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("Hesap2", TransactionTypes.DEBT);
    t2.setAccount(account2);
    transactionRepository.save(t2);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setAccountIds(List.of(account2.getId()));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Hesap2", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Contact ID 0 ile contact null olanları getirmeli")
  void shouldFilterNullContact_whenContactIdIsZero() {
    Contact contact = createContact("Ali");
    Transaction withContact = createTransaction("Kişili", TransactionTypes.DEBT);
    withContact.setContact(contact);
    transactionRepository.save(withContact);
    createTransaction("Kişisiz", TransactionTypes.DEBT);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setContactIds(List.of(0L));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Kişisiz", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Belirli contact ID'lere göre filtreleme")
  void shouldFilterBySpecificContactIds() {
    Contact contact = createContact("Ali");
    Transaction withContact = createTransaction("Kişili", TransactionTypes.DEBT);
    withContact.setContact(contact);
    transactionRepository.save(withContact);
    createTransaction("Kişisiz", TransactionTypes.DEBT);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setContactIds(List.of(contact.getId()));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Kişili", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Contact ID 0 + belirli ID karışık filtreleme")
  void shouldFilterByMixedContactIds() {
    Contact contact = createContact("Ali");
    Transaction withContact = createTransaction("Kişili", TransactionTypes.DEBT);
    withContact.setContact(contact);
    transactionRepository.save(withContact);
    createTransaction("Kişisiz", TransactionTypes.DEBT);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setContactIds(List.of(0L, contact.getId()));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(2, result.getTotalElements());
  }

  @Test
  @DisplayName("Sadece minAmount ile filtreleme")
  void shouldFilterByMinAmountOnly() {
    createTransactionWithAmount("Küçük", BigDecimal.valueOf(100));
    createTransactionWithAmount("Büyük", BigDecimal.valueOf(5000));

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setMinAmount(BigDecimal.valueOf(1000));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Büyük", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Sadece maxAmount ile filtreleme")
  void shouldFilterByMaxAmountOnly() {
    createTransactionWithAmount("Küçük", BigDecimal.valueOf(100));
    createTransactionWithAmount("Büyük", BigDecimal.valueOf(5000));

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setMaxAmount(BigDecimal.valueOf(500));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Küçük", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Sadece minInstallmentCount ile filtreleme")
  void shouldFilterByMinInstallmentCountOnly() {
    Transaction t1 = createTransaction("Az", TransactionTypes.DEBT); t1.setTotalInstallment(2); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Çok", TransactionTypes.DEBT); t2.setTotalInstallment(12); transactionRepository.save(t2);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setMinInstallmentCount(5);

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Çok", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Sadece maxInstallmentCount ile filtreleme")
  void shouldFilterByMaxInstallmentCountOnly() {
    Transaction t1 = createTransaction("Az", TransactionTypes.DEBT); t1.setTotalInstallment(2); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Çok", TransactionTypes.DEBT); t2.setTotalInstallment(12); transactionRepository.save(t2);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setMaxInstallmentCount(5);

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Az", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Min+max installment aralığı")
  void shouldFilterByInstallmentRange() {
    Transaction t1 = createTransaction("Az", TransactionTypes.DEBT); t1.setTotalInstallment(1); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Orta", TransactionTypes.DEBT); t2.setTotalInstallment(6); transactionRepository.save(t2);
    Transaction t3 = createTransaction("Çok", TransactionTypes.DEBT); t3.setTotalInstallment(24); transactionRepository.save(t3);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setMinInstallmentCount(3);
    filter.setMaxInstallmentCount(12);

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Orta", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Sadece startDate ile filtreleme")
  void shouldFilterByStartDateOnly() {
    Transaction t1 = createTransaction("Eski", TransactionTypes.DEBT); t1.setDebtDate(LocalDate.of(2025, 1, 15)); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Yeni", TransactionTypes.DEBT); t2.setDebtDate(LocalDate.of(2025, 8, 15)); transactionRepository.save(t2);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setStartDate(LocalDate.of(2025, 6, 1));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Yeni", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Sadece endDate ile filtreleme")
  void shouldFilterByEndDateOnly() {
    Transaction t1 = createTransaction("Eski", TransactionTypes.DEBT); t1.setDebtDate(LocalDate.of(2025, 1, 15)); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Yeni", TransactionTypes.DEBT); t2.setDebtDate(LocalDate.of(2025, 8, 15)); transactionRepository.save(t2);

    TransactionFilterRequestDto filter = new TransactionFilterRequestDto();
    filter.setEndDate(LocalDate.of(2025, 3, 1));

    Page<Transaction> result = transactionRepository.findAll(TransactionSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals("Eski", result.getContent().get(0).getName());
  }
}
