package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.mappers.TagMapper;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.TransactionTag;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.repositories.TransactionTagRepository;
import com.hakandincturk.services.impl.TagServiceImpl;
import com.hakandincturk.services.rules.TagRules;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

  @InjectMocks
  private TagServiceImpl tagService;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private TransactionTagRepository transactionTagRepository;

  @Mock
  private TagMapper tagMapper;

  @Mock
  private TagRules tagRules;

  @Test
  @DisplayName("Etiket silme - ilişkili transaction tag'ler de soft delete olmalı")
  void deleteTag_shouldSoftDeleteTagAndTransactionTags() {
    Long userId = 1L;
    Long tagId = 5L;

    Tag tag = new Tag();
    tag.setId(tagId);
    tag.setRemoved(false);

    TransactionTag tt1 = new TransactionTag();
    tt1.setId(1L);
    tt1.setRemoved(false);
    TransactionTag tt2 = new TransactionTag();
    tt2.setId(2L);
    tt2.setRemoved(false);

    when(tagRules.checkUserTagExistAndGet(userId, tagId)).thenReturn(tag);
    when(transactionTagRepository.findAllByTagIdAndIsRemovedFalse(tagId))
        .thenReturn(List.of(tt1, tt2));

    tagService.deleteTag(userId, tagId);

    assertTrue(tag.isRemoved());
    assertTrue(tt1.isRemoved());
    assertTrue(tt2.isRemoved());
    verify(transactionTagRepository).saveAll(List.of(tt1, tt2));
    verify(tagRepository).save(tag);
  }

  @Test
  @DisplayName("Etiket silme - ilişkili transaction tag yoksa sadece tag silinmeli")
  void deleteTag_shouldSoftDeleteOnlyTag_whenNoTransactionTags() {
    Long userId = 1L;
    Long tagId = 5L;

    Tag tag = new Tag();
    tag.setId(tagId);
    tag.setRemoved(false);

    when(tagRules.checkUserTagExistAndGet(userId, tagId)).thenReturn(tag);
    when(transactionTagRepository.findAllByTagIdAndIsRemovedFalse(tagId))
        .thenReturn(new ArrayList<>());

    tagService.deleteTag(userId, tagId);

    assertTrue(tag.isRemoved());
    verify(transactionTagRepository, never()).saveAll(any());
    verify(tagRepository).save(tag);
  }

  @Test
  @DisplayName("Etiket silme - transaction tag listesi null olduğunda hata fırlatılmamalı")
  void deleteTag_shouldNotFail_whenTransactionTagsNull() {
    Long userId = 1L;
    Long tagId = 5L;

    Tag tag = new Tag();
    tag.setId(tagId);
    tag.setRemoved(false);

    when(tagRules.checkUserTagExistAndGet(userId, tagId)).thenReturn(tag);
    when(transactionTagRepository.findAllByTagIdAndIsRemovedFalse(tagId))
        .thenReturn(null);

    tagService.deleteTag(userId, tagId);

    assertTrue(tag.isRemoved());
    verify(transactionTagRepository, never()).saveAll(any());
    verify(tagRepository).save(tag);
  }
}
