package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Contact;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.services.rules.ContactRules;

@ExtendWith(MockitoExtension.class)
class ContactRulesTest {

  @InjectMocks
  private ContactRules contactRules;

  @Mock
  private ContactRepository contactRepository;

  @Test
  @DisplayName("Kişi mevcut olduğunda başarıyla döndürülmeli")
  void checkUserContactExistAndGet_shouldReturnContact_whenExists() {
    Long userId = 1L;
    Long contactId = 5L;
    Contact contact = new Contact();
    contact.setId(contactId);

    when(contactRepository.findByIdAndUserIdAndIsRemovedFalse(contactId, userId))
        .thenReturn(Optional.of(contact));

    Contact result = contactRules.checkUserContactExistAndGet(userId, contactId);

    assertNotNull(result);
    assertEquals(contactId, result.getId());
  }

  @Test
  @DisplayName("Kişi bulunamadığında NotFoundException fırlatılmalı")
  void checkUserContactExistAndGet_shouldThrowNotFoundException_whenNotExists() {
    Long userId = 1L;
    Long contactId = 5L;

    when(contactRepository.findByIdAndUserIdAndIsRemovedFalse(contactId, userId))
        .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> contactRules.checkUserContactExistAndGet(userId, contactId));

    assertEquals("Kisi bulunamadı", exception.getMessage());
  }
}
