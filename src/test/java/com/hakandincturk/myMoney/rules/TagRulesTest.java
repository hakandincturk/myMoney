package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Tag;
import com.hakandincturk.repositories.TagRepository;
import com.hakandincturk.services.rules.TagRules;

@ExtendWith(MockitoExtension.class)
class TagRulesTest {

  @InjectMocks
  private TagRules tagRules;

  @Mock
  private TagRepository tagRepository;

  @Test
  @DisplayName("Etiket mevcut olduğunda başarıyla döndürülmeli")
  void checkTagExistAndGet_shouldReturnTag_whenExists() {
    Long tagId = 1L;
    Tag tag = new Tag();
    tag.setId(tagId);

    when(tagRepository.findByIdAndIsRemovedFalse(tagId))
        .thenReturn(Optional.of(tag));

    Tag result = tagRules.checkTagExistAndGet(tagId);

    assertNotNull(result);
    assertEquals(tagId, result.getId());
  }

  @Test
  @DisplayName("Etiket bulunamadığında NotFoundException fırlatılmalı")
  void checkTagExistAndGet_shouldThrowNotFoundException_whenNotExists() {
    Long tagId = 1L;

    when(tagRepository.findByIdAndIsRemovedFalse(tagId))
        .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> tagRules.checkTagExistAndGet(tagId));

    assertEquals("Etiket bulunamadı", exception.getMessage());
  }

  @Test
  @DisplayName("Kullanıcıya ait etiket mevcut olduğunda başarıyla döndürülmeli")
  void checkUserTagExistAndGet_shouldReturnTag_whenExists() {
    Long userId = 1L;
    Long tagId = 5L;
    Tag tag = new Tag();
    tag.setId(tagId);

    when(tagRepository.findByIdAndUserIdAndIsRemovedFalse(tagId, userId))
        .thenReturn(Optional.of(tag));

    Tag result = tagRules.checkUserTagExistAndGet(userId, tagId);

    assertNotNull(result);
    assertEquals(tagId, result.getId());
  }

  @Test
  @DisplayName("Kullanıcıya ait etiket bulunamadığında NotFoundException fırlatılmalı")
  void checkUserTagExistAndGet_shouldThrowNotFoundException_whenNotExists() {
    Long userId = 1L;
    Long tagId = 5L;

    when(tagRepository.findByIdAndUserIdAndIsRemovedFalse(tagId, userId))
        .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> tagRules.checkUserTagExistAndGet(userId, tagId));

    assertEquals("Etiket bulunamadı", exception.getMessage());
  }

  @Test
  @DisplayName("Tüm etiket ID'leri mevcut olduğunda liste döndürülmeli")
  void checkAllIdsAndGet_shouldReturnTags_whenAllExist() {
    List<Long> ids = List.of(1L, 2L);
    Tag tag1 = new Tag();
    tag1.setId(1L);
    Tag tag2 = new Tag();
    tag2.setId(2L);

    when(tagRepository.findAllByIdInAndIsRemovedFalse(ids))
        .thenReturn(List.of(tag1, tag2));

    List<Tag> result = tagRules.checkAllIdsAndGet(ids);

    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("Bazı etiketler bulunamadığında NotFoundException fırlatılmalı")
  void checkAllIdsAndGet_shouldThrowNotFoundException_whenSomeMissing() {
    List<Long> ids = List.of(1L, 2L, 3L);
    Tag tag1 = new Tag();
    tag1.setId(1L);

    when(tagRepository.findAllByIdInAndIsRemovedFalse(ids))
        .thenReturn(List.of(tag1));

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> tagRules.checkAllIdsAndGet(ids));

    assertEquals("Aranan etiketlerin hepsi bulunamadı", exception.getMessage());
  }

  @Test
  @DisplayName("ID listesi null olduğunda boş liste döndürülmeli")
  void checkAllIdsAndGet_shouldReturnEmptyList_whenIdsNull() {
    List<Tag> result = tagRules.checkAllIdsAndGet(null);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("ID listesi boş olduğunda boş liste döndürülmeli")
  void checkAllIdsAndGet_shouldReturnEmptyList_whenIdsEmpty() {
    List<Tag> result = tagRules.checkAllIdsAndGet(new ArrayList<>());

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
