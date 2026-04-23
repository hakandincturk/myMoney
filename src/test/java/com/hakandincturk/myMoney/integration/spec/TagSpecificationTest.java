package com.hakandincturk.myMoney.integration.spec;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.specs.TagSpecification;
import com.hakandincturk.dtos.tag.request.FilterListUserTags;
import com.hakandincturk.dtos.tag.response.ListUserTagsWithTransactionCountDto;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.models.Users;

class TagSpecificationTest extends BaseSpecificationTest {

  // --- TagSpecification ---

  @Test
  @DisplayName("Tag filter - isim ile filtreleme")
  void shouldFilterByName() {
    createTag("Yemek");
    createTag("Ulaşım");
    createTag("Eğlence");

    FilterListUserTags filter = new FilterListUserTags();
    filter.setName("yem");

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("Yemek", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Tag filter - boş filtre tüm etiketleri getirmeli")
  void emptyFilter_shouldReturnAll() {
    createTag("Yemek");
    createTag("Ulaşım");

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(testUser.getId(), new FilterListUserTags()), PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  @DisplayName("Tag filter - silinmiş etiketleri getirmemeli")
  void shouldNotReturnRemovedTags() {
    createTag("Aktif");
    Tag removed = createTag("Silinmiş");
    removed.setRemoved(true);
    tagRepository.save(removed);

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(testUser.getId(), new FilterListUserTags()), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("Aktif", result.getContent().get(0).getName());
  }

  @Test
  @DisplayName("Tag filter - başka kullanıcının etiketlerini getirmemeli")
  void shouldNotReturnOtherUsersTags() {
    createTag("Benim Etiketim");

    Users otherUser = new Users();
    otherUser.setFullName("Other");
    otherUser.setEmail("other-tag@test.com");
    otherUser.setPassword("pass");
    otherUser.setPhone("111");
    otherUser = userRepository.save(otherUser);

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(otherUser.getId(), new FilterListUserTags()), PageRequest.of(0, 10));

    assertEquals(0, result.getTotalElements());
  }

  @Test
  @DisplayName("Tag filter - sadece başlangıç tarihi ile filtreleme")
  void shouldFilterByCreatedStartDateOnly() {
    createTag("Eski");
    createTag("Yeni");

    FilterListUserTags filter = new FilterListUserTags();
    filter.setCreatedStartDate(LocalDate.now().minusDays(1));

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  @DisplayName("Tag filter - sadece bitiş tarihi ile filtreleme")
  void shouldFilterByCreatedEndDateOnly() {
    createTag("Etiket");

    FilterListUserTags filter = new FilterListUserTags();
    filter.setCreatedEndDate(LocalDate.now().plusDays(1));

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Tag filter - tarih aralığı ile filtreleme")
  void shouldFilterByDateRange() {
    createTag("Bugünkü");

    FilterListUserTags filter = new FilterListUserTags();
    filter.setCreatedStartDate(LocalDate.now().minusDays(1));
    filter.setCreatedEndDate(LocalDate.now().plusDays(1));

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Tag filter - geçmiş tarih aralığı ile boş sonuç")
  void shouldReturnEmpty_whenDateRangeInPast() {
    createTag("Bugünkü");

    FilterListUserTags filter = new FilterListUserTags();
    filter.setCreatedStartDate(LocalDate.of(2020, 1, 1));
    filter.setCreatedEndDate(LocalDate.of(2020, 12, 31));

    Page<Tag> result = tagRepository.findAll(TagSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));

    assertEquals(0, result.getTotalElements());
  }

  // --- TagCustomRepository (findWithTransactionCount) ---

  @Test
  @DisplayName("TagCustomRepo - transaction sayısı ile etiketler getirilmeli")
  void customRepo_shouldReturnTagsWithTransactionCount() {
    Tag tag1 = createTag("Yemek");
    Tag tag2 = createTag("Ulaşım");
    Transaction t1 = createTransaction("Market", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("Taksi", TransactionTypes.PAYMENT);
    createTransactionTag(t1, tag1);
    createTransactionTag(t2, tag1);
    createTransactionTag(t2, tag2);

    Specification<Tag> spec = TagSpecification.filter(testUser.getId(), new FilterListUserTags());
    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(spec, PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
    var yemek = result.getContent().stream().filter(t -> "Yemek".equals(t.getName())).findFirst().orElseThrow();
    var ulasim = result.getContent().stream().filter(t -> "Ulaşım".equals(t.getName())).findFirst().orElseThrow();
    assertEquals(2, yemek.getTransactionCount());
    assertEquals(1, ulasim.getTransactionCount());
  }

  @Test
  @DisplayName("TagCustomRepo - transaction olmayan etiket count 0 olmalı")
  void customRepo_shouldReturnZeroCount_whenNoTransactions() {
    createTag("Boş Etiket");

    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(
        TagSpecification.filter(testUser.getId(), new FilterListUserTags()), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals(0, result.getContent().get(0).getTransactionCount());
  }

  @Test
  @DisplayName("TagCustomRepo - isim filtresi ile çalışmalı")
  void customRepo_shouldWorkWithNameFilter() {
    Tag tag1 = createTag("Yemek");
    createTag("Ulaşım");
    Transaction t1 = createTransaction("Market", TransactionTypes.DEBT);
    createTransactionTag(t1, tag1);

    FilterListUserTags filter = new FilterListUserTags();
    filter.setName("yem");

    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(
        TagSpecification.filter(testUser.getId(), filter), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("Yemek", result.getContent().get(0).getName());
    assertEquals(1, result.getContent().get(0).getTransactionCount());
  }

  @Test
  @DisplayName("TagCustomRepo - sayfalama doğru çalışmalı")
  void customRepo_shouldPaginateCorrectly() {
    for (int i = 1; i <= 5; i++) createTag("Etiket " + i);

    Specification<Tag> spec = TagSpecification.filter(testUser.getId(), new FilterListUserTags());
    Page<ListUserTagsWithTransactionCountDto> page1 = tagRepository.findWithTransactionCount(spec, PageRequest.of(0, 2));
    Page<ListUserTagsWithTransactionCountDto> page2 = tagRepository.findWithTransactionCount(spec, PageRequest.of(1, 2));

    assertEquals(5, page1.getTotalElements());
    assertEquals(2, page1.getContent().size());
    assertEquals(2, page2.getContent().size());
    assertNotEquals(page1.getContent().get(0).getId(), page2.getContent().get(0).getId());
  }

  @Test
  @DisplayName("TagCustomRepo - transactionCount'a göre sıralama")
  void customRepo_shouldSortByTransactionCount() {
    Tag tagAz = createTag("Az");
    Tag tagCok = createTag("Çok");
    Transaction t1 = createTransaction("İ1", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("İ2", TransactionTypes.DEBT);
    Transaction t3 = createTransaction("İ3", TransactionTypes.DEBT);
    createTransactionTag(t1, tagAz);
    createTransactionTag(t1, tagCok);
    createTransactionTag(t2, tagCok);
    createTransactionTag(t3, tagCok);

    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(
        TagSpecification.filter(testUser.getId(), new FilterListUserTags()),
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "transactionCount")));

    assertEquals("Çok", result.getContent().get(0).getName());
    assertEquals(3, result.getContent().get(0).getTransactionCount());
  }

  @Test
  @DisplayName("TagCustomRepo - isme göre sıralama")
  void customRepo_shouldSortByName() {
    createTag("Zeytin");
    createTag("Araba");
    createTag("Market");

    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(
        TagSpecification.filter(testUser.getId(), new FilterListUserTags()),
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

    assertEquals("Araba", result.getContent().get(0).getName());
    assertEquals("Zeytin", result.getContent().get(2).getName());
  }

  @Test
  @DisplayName("TagCustomRepo - boş sonuç")
  void customRepo_shouldReturnEmpty_whenNoTags() {
    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(
        TagSpecification.filter(testUser.getId(), new FilterListUserTags()), PageRequest.of(0, 10));

    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  @DisplayName("TagCustomRepo - silinmiş transaction tag'ler sayılmamalı")
  void customRepo_shouldNotCountRemovedTransactionTags() {
    Tag tag = createTag("Test");
    Transaction t1 = createTransaction("İ1", TransactionTypes.DEBT);
    Transaction t2 = createTransaction("İ2", TransactionTypes.DEBT);
    createTransactionTag(t1, tag);
    TransactionTag removedTt = createTransactionTag(t2, tag);
    removedTt.setRemoved(true);
    transactionTagRepository.save(removedTt);

    Page<ListUserTagsWithTransactionCountDto> result = tagRepository.findWithTransactionCount(
        TagSpecification.filter(testUser.getId(), new FilterListUserTags()), PageRequest.of(0, 10));

    assertEquals(1, result.getContent().get(0).getTransactionCount());
  }
}
