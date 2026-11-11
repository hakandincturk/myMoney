package com.hakandincturk.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.enums.TransactionTypes;
import com.hakandincturk.core.enums.sort.TagTransactionColumn;
import com.hakandincturk.core.enums.sort.TransactionSortColumn;
import com.hakandincturk.core.events.TransactionCreatedEvent;
import com.hakandincturk.core.specs.TagTransactionFilterSpecification;
import com.hakandincturk.core.specs.TransactionSpecification;
import com.hakandincturk.dtos.transaction.request.CreateTransactionRequestDto;
import com.hakandincturk.dtos.transaction.request.TagTransactionsFilterRequestDto;
import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.dtos.transaction.request.UpdateTransactionTagsRequestDto;
import com.hakandincturk.dtos.transaction.response.ListTagTransactionsResponseDto;
import com.hakandincturk.dtos.transaction.response.ListInstallments;
import com.hakandincturk.dtos.transaction.response.ListMyTransactionsResponseDto;
import com.hakandincturk.dtos.transaction.response.UpdateTransactionTagsResponseDto;
import com.hakandincturk.dtos.tag.response.TransactionTagInfoDto;
import com.hakandincturk.factories.AccountFactory;
import com.hakandincturk.factories.TagFactory;
import com.hakandincturk.factories.TransactionFactory;
import com.hakandincturk.mappers.InstallmentMapper;
import com.hakandincturk.mappers.TagMapper;
import com.hakandincturk.mappers.TransactionTagMapper;
import com.hakandincturk.mappers.TransactionMapper;
import com.hakandincturk.models.Account;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Transaction;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.AccountRepository;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.repositories.TransactionRepository;
import com.hakandincturk.services.abstracts.TransactionService;
import com.hakandincturk.services.rules.TagRules;
import com.hakandincturk.services.rules.TransactionRules;
import com.hakandincturk.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  // Repositories
  private final AccountRepository accountRepository;
  private final TagRepository tagRepository;
  private final TransactionRepository transactionRepository;
  private final InstallmentRepository installmentRepository;
  private final TransactionTagRepository transactionTagRepository;

  // Mappers
  private final TransactionMapper transactionMapper;
  private final InstallmentMapper installmentMapper;
  private final TransactionTagMapper transactionTagMapper;
  private final TagMapper tagMapper;

  // Factories
  private final TransactionFactory transactionFactory;
  private final TagFactory tagFactory;
  private final AccountFactory accountFactory;

  // Rules
  private final TagRules tagRules;
  private final TransactionRules transactionRules;

  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void createTransaction(Long userId, CreateTransactionRequestDto body) {

    transactionRules.validateCreateTransactionRequest(body);

    Users activeUser = transactionRules.getValidatedUser(userId);
    Account account = transactionRules.getValidatedAccount(userId, body.getAccountId());
    Contact contact = transactionRules.getValidatedContact(userId, body.getContactId());

    List<Long> tagIds = body.getTag() != null ? body.getTag().getTagIds() : null;
    List<String> newTagNames = body.getTag() != null ? body.getTag().getNewTags() : null;
    List<Tag> tags = this.resolveTags(userId, activeUser, tagIds, newTagNames);

    Transaction newTransaction = transactionFactory.createTransaction(body, activeUser, account, contact, tags);
    transactionRepository.save(newTransaction);

    if(newTransaction.getType().equals(TransactionTypes.DEBT)) {
      account = accountFactory.reCalculateBalanceOnTransactionCreate(account, newTransaction.getType(), newTransaction.getTotalAmount());
      accountRepository.save(account);
    }

    eventPublisher.publishEvent(new TransactionCreatedEvent(newTransaction));
  }

  @Override
  @Transactional
  public UpdateTransactionTagsResponseDto updateTransactionTags(Long userId, Long transactionId, UpdateTransactionTagsRequestDto body) {
    Transaction transaction = transactionRules.checkUserTransactionExistAndGet(userId, transactionId);

    List<Tag> tags = this.resolveTags(userId, transaction.getUser(), body.getTagIds(), body.getNewTags());

    List<TransactionTag> changedLinks = transactionFactory.syncTransactionTags(transaction, tags);
    transactionTagRepository.saveAll(changedLinks);

    List<TransactionTagInfoDto> tagInfos = tags.stream().map(tagMapper::toTransactionTagInfoDto).toList();
    return new UpdateTransactionTagsResponseDto(transaction.getId(), tagInfos);
  }

  /**
   * Turns the selected tag ids and typed tag names into the final tag list.
   * Ids must belong to the user; a typed name matching an existing tag reuses it, so duplicates are never created.
   * Only genuinely new tags are saved here.
  */
  private List<Tag> resolveTags(Long userId, Users user, List<Long> tagIds, List<String> newTagNames){
    Map<Long, Tag> tagsById = new LinkedHashMap<>();
    tagRules.checkAllUserTagIdsAndGet(userId, tagIds).forEach(tag -> tagsById.put(tag.getId(), tag));

    if(newTagNames == null || newTagNames.isEmpty()){
      return new ArrayList<>(tagsById.values());
    }

    List<Tag> existingUserTags = tagRepository.findAllByUserIdAndIsRemovedFalse(userId);
    List<Tag> namedTags = tagFactory.resolveTagsByName(newTagNames, existingUserTags, user);

    List<Tag> createdTags = namedTags.stream().filter(tag -> tag.getId() == null).toList();
    tagRepository.saveAll(createdTags);

    namedTags.forEach(tag -> tagsById.putIfAbsent(tag.getId(), tag));
    return new ArrayList<>(tagsById.values());
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
    .map(installmentMapper::toListTransactionIntallments)
    .toList();

    return installments;
  }

  @Override
  public Page<ListTagTransactionsResponseDto> listTagTransactions(Long userId, Long tagId, TagTransactionsFilterRequestDto pageData) {
    tagRules.checkTagExistAndGet(tagId);

    Specification<TransactionTag> specs = TagTransactionFilterSpecification.filter(userId, tagId, pageData);
    Pageable pageable = PaginationUtils.toPageable(pageData, TagTransactionColumn.class);
    Page<TransactionTag> dbTransactionTags = transactionTagRepository.findAll(specs, pageable);

    return dbTransactionTags.map(transactionTagMapper::toListTagTransactionsResponseDto);
  }
}
