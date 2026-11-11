package com.hakandincturk.myMoney.service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.dtos.tag.response.TransactionTagInfoDto;
import com.hakandincturk.dtos.transaction.response.UpdateTransactionTagsResponseDto;
import com.hakandincturk.dtos.transaction.request.UpdateTransactionTagsRequestDto;
import com.hakandincturk.mappers.TagMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.events.TransactionCreatedEvent;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.CreateTransactionTagDetail;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.factories.TagFactory;
import com.hakandincturk.factories.TransactionFactory;
import com.hakandincturk.mappers.InstallmentMapper;
import com.hakandincturk.mappers.TransactionMapper;
import com.hakandincturk.mappers.TransactionTagMapper;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.services.impl.TransactionServiceImpl;
import com.hakandincturk.services.rules.TagRules;
import com.hakandincturk.services.rules.TransactionRules;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @InjectMocks
  private TransactionServiceImpl transactionService;

  @Mock
  private AccountRepository accountRepository;
  @Mock
  private TagRepository tagRepository;
  @Mock
  private TransactionRepository transactionRepository;
  @Mock
  private InstallmentRepository installmentRepository;
  @Mock
  private TransactionTagRepository transactionTagRepository;
  @Mock
  private TransactionMapper transactionMapper;
  @Mock
  private InstallmentMapper installmentMapper;
  @Mock
  private TransactionTagMapper transactionTagMapper;
  @Mock
  private TagMapper tagMapper;
  @Mock
  private TransactionFactory transactionFactory;
  @Mock
  private TagFactory tagFactory;
  @Mock
  private AccountFactory accountFactory;
  @Mock
  private TagRules tagRules;
  @Mock
  private TransactionRules transactionRules;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Test
  @DisplayName("Başarılı transaction oluşturma - DEBT tipi ile hesap bakiyesi güncellenmeli")
  void createTransaction_debt_shouldUpdateAccountBalance() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Account account = new Account();
    account.setId(10L);

    CreateTransactionTagDetail tagDetail = new CreateTransactionTagDetail(List.of(), List.of());
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.DEBT);
    body.setTotalAmount(BigDecimal.valueOf(1000));
    body.setTotalInstallment(3);
    body.setAccountId(10L);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setName("Test");
    body.setTag(tagDetail);

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.DEBT);
    transaction.setTotalAmount(BigDecimal.valueOf(1000));

    when(transactionRules.getValidatedUser(userId)).thenReturn(user);
    when(transactionRules.getValidatedAccount(userId, 10L)).thenReturn(account);
    when(transactionRules.getValidatedContact(userId, null)).thenReturn(null);
    when(transactionFactory.createTransaction(body, user, account, null, List.of())).thenReturn(transaction);
    when(accountFactory.reCalculateBalanceOnTransactionCreate(account, TransactionTypes.DEBT, BigDecimal.valueOf(1000)))
        .thenReturn(account);

    transactionService.createTransaction(userId, body);

    verify(transactionRules).validateCreateTransactionRequest(body);
    verify(transactionRepository).save(transaction);
    verify(accountRepository).save(account);
    verify(eventPublisher).publishEvent(any(TransactionCreatedEvent.class));
  }

  @Test
  @DisplayName("CREDIT tipi transaction oluşturma - hesap bakiyesi güncellenmemeli")
  void createTransaction_credit_shouldNotUpdateAccountBalance() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Account account = new Account();
    account.setId(10L);

    CreateTransactionTagDetail tagDetail = new CreateTransactionTagDetail(List.of(), List.of());
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.CREDIT);
    body.setTotalAmount(BigDecimal.valueOf(2000));
    body.setTotalInstallment(1);
    body.setAccountId(10L);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setName("Alacak");
    body.setTag(tagDetail);

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.CREDIT);
    transaction.setTotalAmount(BigDecimal.valueOf(2000));

    when(transactionRules.getValidatedUser(userId)).thenReturn(user);
    when(transactionRules.getValidatedAccount(userId, 10L)).thenReturn(account);
    when(transactionRules.getValidatedContact(userId, null)).thenReturn(null);
    when(transactionFactory.createTransaction(body, user, account, null, List.of())).thenReturn(transaction);

    transactionService.createTransaction(userId, body);

    verify(accountFactory, never()).reCalculateBalanceOnTransactionCreate(any(), any(), any());
    verify(accountRepository, never()).save(any());
    verify(eventPublisher).publishEvent(any(TransactionCreatedEvent.class));
  }

  @Test
  @DisplayName("Transaction silme - taksitler de soft delete olmalı")
  void deleteMyTransaction_shouldSoftDeleteTransactionAndInstallments() {
    Long userId = 1L;
    Long transactionId = 100L;

    Installment i1 = new Installment();
    i1.setId(1L);
    Installment i2 = new Installment();
    i2.setId(2L);

    Transaction transaction = new Transaction();
    transaction.setId(transactionId);
    transaction.setInstallments(List.of(i1, i2));

    when(transactionRules.checkUserTransactionExistAndGet(userId, transactionId)).thenReturn(transaction);

    transactionService.deleteMyTransaction(userId, transactionId);

    assertTrue(transaction.isRemoved());
    assertTrue(i1.isRemoved());
    assertTrue(i2.isRemoved());
    verify(installmentRepository).saveAll(transaction.getInstallments());
    verify(transactionRepository).save(transaction);
  }

  @Test
  @DisplayName("Transaction silme - taksit yoksa sadece transaction silinmeli")
  void deleteMyTransaction_shouldSoftDeleteOnlyTransaction_whenNoInstallments() {
    Long userId = 1L;
    Long transactionId = 100L;

    Transaction transaction = new Transaction();
    transaction.setId(transactionId);
    transaction.setInstallments(new ArrayList<>());

    when(transactionRules.checkUserTransactionExistAndGet(userId, transactionId)).thenReturn(transaction);

    transactionService.deleteMyTransaction(userId, transactionId);

    assertTrue(transaction.isRemoved());
    verify(installmentRepository, never()).saveAll(any());
    verify(transactionRepository).save(transaction);
  }

  @Test
  @DisplayName("Transaction taksitlerini listeleme")
  void listTransactionInstallments_shouldReturnSortedInstallments() {
    Long userId = 1L;
    Long transactionId = 100L;

    Installment i1 = new Installment();
    i1.setId(2L);
    i1.setInstallmentNumber(2);
    Installment i2 = new Installment();
    i2.setId(1L);
    i2.setInstallmentNumber(1);

    Transaction transaction = new Transaction();
    transaction.setId(transactionId);
    transaction.setInstallments(new ArrayList<>(List.of(i1, i2)));

    ListInstallments dto1 = new ListInstallments();
    ListInstallments dto2 = new ListInstallments();

    when(transactionRules.checkUserTransactionExistAndGet(userId, transactionId)).thenReturn(transaction);
    when(installmentMapper.toListTransactionIntallments(i2)).thenReturn(dto1);
    when(installmentMapper.toListTransactionIntallments(i1)).thenReturn(dto2);

    List<ListInstallments> result = transactionService.listTransactionInstallments(userId, transactionId);

    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("Yeni tag'ler ile transaction oluşturma")
  void createTransaction_withNewTags_shouldSaveNewTags() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Account account = new Account();
    account.setId(10L);

    Tag newTag = new Tag();
    newTag.setName("Yeni Etiket");

    CreateTransactionTagDetail tagDetail = new CreateTransactionTagDetail(List.of(), List.of("Yeni Etiket"));
    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.PAYMENT);
    body.setTotalAmount(BigDecimal.valueOf(500));
    body.setTotalInstallment(1);
    body.setAccountId(10L);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setName("Test");
    body.setTag(tagDetail);

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.PAYMENT);

    when(transactionRules.getValidatedUser(userId)).thenReturn(user);
    when(transactionRules.getValidatedAccount(userId, 10L)).thenReturn(account);
    when(transactionRules.getValidatedContact(userId, null)).thenReturn(null);
    when(tagRepository.findAllByUserIdAndIsRemovedFalse(userId)).thenReturn(List.of());
    when(tagFactory.resolveTagsByName(List.of("Yeni Etiket"), List.of(), user)).thenReturn(List.of(newTag));
    when(transactionFactory.createTransaction(eq(body), eq(user), eq(account), isNull(), any())).thenReturn(transaction);

    transactionService.createTransaction(userId, body);

    verify(tagRepository).saveAll(List.of(newTag));
    verify(transactionRepository).save(transaction);
  }

  @Test
  @DisplayName("Var olan etiket adı yazıldığında yeni etiket kaydedilmemeli, mevcut etiket bağlanmalı")
  void createTransaction_withExistingTagName_shouldReuseTagWithoutSaving() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Account account = new Account();
    account.setId(10L);
    Tag existing = new Tag();
    existing.setId(7L);
    existing.setName("aylık abonelik");

    CreateTransactionRequestDto body = new CreateTransactionRequestDto();
    body.setType(TransactionTypes.PAYMENT);
    body.setTotalAmount(BigDecimal.valueOf(229));
    body.setTotalInstallment(1);
    body.setAccountId(10L);
    body.setDebtDate(LocalDate.of(2025, 6, 1));
    body.setName("Netflix");
    body.setTag(new CreateTransactionTagDetail(List.of(7L), List.of("Aylık Abonelik")));

    Transaction transaction = new Transaction();
    transaction.setType(TransactionTypes.PAYMENT);

    when(transactionRules.getValidatedUser(userId)).thenReturn(user);
    when(transactionRules.getValidatedAccount(userId, 10L)).thenReturn(account);
    when(tagRules.checkAllUserTagIdsAndGet(userId, List.of(7L))).thenReturn(List.of(existing));
    when(tagRepository.findAllByUserIdAndIsRemovedFalse(userId)).thenReturn(List.of(existing));
    when(tagFactory.resolveTagsByName(List.of("Aylık Abonelik"), List.of(existing), user)).thenReturn(List.of(existing));
    when(transactionFactory.createTransaction(eq(body), eq(user), eq(account), isNull(), any())).thenReturn(transaction);

    transactionService.createTransaction(userId, body);

    verify(tagRepository).saveAll(List.of());
    // Selected id and typed name point to the same tag: it is linked once
    verify(transactionFactory).createTransaction(eq(body), eq(user), eq(account), isNull(), eq(List.of(existing)));
  }

  @Test
  @DisplayName("Etiket güncelleme - seçili ve yeni etiketler birleştirilip bağlar senkronlanmalı")
  void updateTransactionTags_shouldResolveTagsAndSyncLinks() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);
    Transaction transaction = new Transaction();
    transaction.setId(40L);
    transaction.setUser(user);
    Tag selected = new Tag();
    selected.setId(3L);
    selected.setName("Ev");
    Tag created = new Tag();
    created.setName("Market");

    UpdateTransactionTagsRequestDto body = new UpdateTransactionTagsRequestDto(List.of(3L), List.of("Market"));
    TransactionTag changedLink = new TransactionTag();

    when(transactionRules.checkUserTransactionExistAndGet(userId, 40L)).thenReturn(transaction);
    when(tagRules.checkAllUserTagIdsAndGet(userId, List.of(3L))).thenReturn(List.of(selected));
    when(tagRepository.findAllByUserIdAndIsRemovedFalse(userId)).thenReturn(List.of(selected));
    when(tagFactory.resolveTagsByName(List.of("Market"), List.of(selected), user)).thenReturn(List.of(created));
    when(tagRepository.saveAll(List.of(created))).thenAnswer(invocation -> {
      created.setId(8L);
      return List.of(created);
    });
    when(transactionFactory.syncTransactionTags(eq(transaction), anyList())).thenReturn(List.of(changedLink));
    when(tagMapper.toTransactionTagInfoDto(selected)).thenReturn(new TransactionTagInfoDto(3L, "Ev"));
    when(tagMapper.toTransactionTagInfoDto(created)).thenReturn(new TransactionTagInfoDto(8L, "Market"));

    UpdateTransactionTagsResponseDto response = transactionService.updateTransactionTags(userId, 40L, body);

    verify(transactionFactory).syncTransactionTags(transaction, List.of(selected, created));
    verify(transactionTagRepository).saveAll(List.of(changedLink));
    assertEquals(40L, response.getTransactionId());
    assertEquals(List.of(3L, 8L), response.getTags().stream().map(TransactionTagInfoDto::getId).toList());
  }

  @Test
  @DisplayName("Etiket güncelleme - işlem kullanıcıya ait değilse hiçbir değişiklik yapılmamalı")
  void updateTransactionTags_shouldNotTouchTags_whenTransactionNotFound() {
    when(transactionRules.checkUserTransactionExistAndGet(1L, 40L)).thenThrow(new NotFoundException("İşlem bulunamadı"));

    assertThrows(NotFoundException.class,
        () -> transactionService.updateTransactionTags(1L, 40L, new UpdateTransactionTagsRequestDto(List.of(), List.of("X"))));

    verifyNoInteractions(tagRepository, transactionTagRepository, tagFactory);
  }
}
