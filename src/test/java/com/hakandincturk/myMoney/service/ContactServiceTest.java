package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.mappers.ContactMapper;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.services.impl.ContactServiceImpl;
import com.hakandincturk.services.rules.ContactRules;
import com.hakandincturk.services.rules.UserRules;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

  @InjectMocks
  private ContactServiceImpl contactService;

  @Mock
  private ContactRepository contactRepository;

  @Mock
  private ContactRules contactRules;

  @Mock
  private UserRules userRules;

  @Mock
  private ContactMapper contactMapper;

  @Test
  @DisplayName("Başarılı kişi oluşturma")
  void createAccount_shouldSaveContact() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    CreateContactRequestDto body = new CreateContactRequestDto("Ali Veli", "Notum");

    when(userRules.checkUserExistAndGet(userId)).thenReturn(user);

    contactService.createAccount(userId, body);

    ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
    verify(contactRepository).save(captor.capture());

    Contact saved = captor.getValue();
    assertEquals("Ali Veli", saved.getFullName());
    assertEquals("Notum", saved.getNote());
    assertEquals(user, saved.getUser());
  }

  @Test
  @DisplayName("Başarılı kişi güncelleme")
  void updateMyContact_shouldUpdateAndSave() {
    Long userId = 1L;
    Long contactId = 5L;

    Contact dbContact = new Contact();
    dbContact.setId(contactId);
    dbContact.setFullName("Eski İsim");
    dbContact.setNote("Eski Not");

    UpdateMyContactRequestDto body = new UpdateMyContactRequestDto("Yeni İsim", "Yeni Not");

    when(contactRules.checkUserContactExistAndGet(userId, contactId)).thenReturn(dbContact);

    contactService.updateMyContact(userId, contactId, body);

    verify(contactRepository).save(dbContact);
    assertEquals("Yeni İsim", dbContact.getFullName());
    assertEquals("Yeni Not", dbContact.getNote());
  }

  @Test
  @DisplayName("Başarılı kişi silme (soft delete)")
  void deleteContact_shouldSoftDelete() {
    Long userId = 1L;
    Long contactId = 5L;

    Contact dbContact = new Contact();
    dbContact.setId(contactId);
    dbContact.setRemoved(false);

    when(contactRules.checkUserContactExistAndGet(userId, contactId)).thenReturn(dbContact);

    contactService.deleteContact(userId, contactId);

    assertTrue(dbContact.isRemoved());
    verify(contactRepository).save(dbContact);
  }
}
