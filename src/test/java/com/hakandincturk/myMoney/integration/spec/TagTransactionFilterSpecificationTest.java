package com.hakandincturk.myMoney.integration.spec;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.specs.TagTransactionFilterSpecification;
import com.hakandincturk.dtos.transaction.request.TagTransactionsFilterRequestDto;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;

class TagTransactionFilterSpecificationTest extends BaseSpecificationTest {

  @Test
  @DisplayName("Boş filtre ile temel çalışma")
  void shouldReturnBasicResults() {
    Tag tag = createTag("Yemek");
    Transaction t1 = createTransaction("Market", TransactionTypes.DEBT);
    createTransactionTag(t1, tag);

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), new TagTransactionsFilterRequestDto()), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Silinmiş TransactionTag getirmemeli")
  void shouldNotReturnRemovedTransactionTags() {
    Tag tag = createTag("Test");
    Transaction t1 = createTransaction("Aktif", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("Silinmiş", TransactionTypes.DEBT);
    createTransactionTag(t1, tag);
    TransactionTag removed = createTransactionTag(t2, tag);
    removed.setRemoved(true);
    transactionTagRepository.save(removed);

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), new TagTransactionsFilterRequestDto()), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Transaction ismine göre filtreleme")
  void shouldFilterByTransactionName() {
    Tag tag = createTag("Genel");
    Transaction t1 = createTransaction("Market Alışverişi", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("Kira Ödemesi", TransactionTypes.PAYMENT);
    createTransactionTag(t1, tag);
    createTransactionTag(t2, tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setTransactionName("market");

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Hesap ID'ye göre filtreleme")
  void shouldFilterByAccountIds() {
    Account account2 = createSecondAccount("İkinci Hesap");
    Tag tag = createTag("Test");
    Transaction t1 = createTransaction("Hesap1", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("Hesap2", TransactionTypes.DEBT);
    t2.setAccount(account2);
    transactionRepository.save(t2);
    createTransactionTag(t1, tag);
    createTransactionTag(t2, tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setAccountIds(List.of(account2.getId()));

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Min+max tutar aralığı")
  void shouldFilterByAmountRange() {
    Tag tag = createTag("Test");
    createTransactionTag(createTransactionWithAmount("Küçük", BigDecimal.valueOf(100)), tag);
    createTransactionTag(createTransactionWithAmount("Orta", BigDecimal.valueOf(500)), tag);
    createTransactionTag(createTransactionWithAmount("Büyük", BigDecimal.valueOf(5000)), tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setMinAmount(BigDecimal.valueOf(200));
    filter.setMaxAmount(BigDecimal.valueOf(1000));

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Sadece minAmount ile filtreleme")
  void shouldFilterByMinAmountOnly() {
    Tag tag = createTag("Test");
    createTransactionTag(createTransactionWithAmount("Küçük", BigDecimal.valueOf(100)), tag);
    createTransactionTag(createTransactionWithAmount("Büyük", BigDecimal.valueOf(5000)), tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setMinAmount(BigDecimal.valueOf(1000));

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Sadece maxAmount ile filtreleme")
  void shouldFilterByMaxAmountOnly() {
    Tag tag = createTag("Test");
    createTransactionTag(createTransactionWithAmount("Küçük", BigDecimal.valueOf(100)), tag);
    createTransactionTag(createTransactionWithAmount("Büyük", BigDecimal.valueOf(5000)), tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setMaxAmount(BigDecimal.valueOf(500));

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Min+max taksit aralığı")
  void shouldFilterByInstallmentRange() {
    Tag tag = createTag("Test");
    Transaction t1 = createTransaction("Az", TransactionTypes.DEBT); t1.setTotalInstallment(2); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Çok", TransactionTypes.DEBT); t2.setTotalInstallment(12); transactionRepository.save(t2);
    createTransactionTag(t1, tag);
    createTransactionTag(t2, tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setMinInstallmentCount(5);
    filter.setMaxInstallmentCount(15);

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Sadece minInstallmentCount")
  void shouldFilterByMinInstallmentOnly() {
    Tag tag = createTag("Test");
    Transaction t1 = createTransaction("Az", TransactionTypes.DEBT); t1.setTotalInstallment(1); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Çok", TransactionTypes.DEBT); t2.setTotalInstallment(10); transactionRepository.save(t2);
    createTransactionTag(t1, tag);
    createTransactionTag(t2, tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setMinInstallmentCount(5);

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Sadece maxInstallmentCount")
  void shouldFilterByMaxInstallmentOnly() {
    Tag tag = createTag("Test");
    Transaction t1 = createTransaction("Az", TransactionTypes.DEBT); t1.setTotalInstallment(1); transactionRepository.save(t1);
    Transaction t2 = createTransaction("Çok", TransactionTypes.DEBT); t2.setTotalInstallment(10); transactionRepository.save(t2);
    createTransactionTag(t1, tag);
    createTransactionTag(t2, tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setMaxInstallmentCount(5);

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Tipe göre filtreleme")
  void shouldFilterByType() {
    Tag tag = createTag("Test");
    createTransactionTag(createTransaction("Borç", TransactionTypes.DEBT), tag);
    createTransactionTag(createTransaction("Alacak", TransactionTypes.CREDIT), tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setTypes(List.of(TransactionTypes.CREDIT));

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Statüye göre filtreleme")
  void shouldFilterByStatus() {
    Tag tag = createTag("Test");
    createTransactionTag(createTransaction("Bekleyen", TransactionTypes.DEBT), tag);
    Transaction t2 = createTransaction("Ödenen", TransactionTypes.DEBT);
    t2.setStatus(TransactionStatuses.PAID);
    transactionRepository.save(t2);
    createTransactionTag(t2, tag);

    TagTransactionsFilterRequestDto filter = new TagTransactionsFilterRequestDto();
    filter.setStatuses(List.of(TransactionStatuses.PAID));

    Page<TransactionTag> result = transactionTagRepository.findAll(
        TagTransactionFilterSpecification.filter(testUser.getId(), tag.getId(), filter), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
  }
}
