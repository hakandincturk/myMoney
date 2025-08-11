package com.hakandincturk.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.services.abstracts.ContactService;
import com.hakandincturk.services.rules.ContactRules;
import com.hakandincturk.services.rules.UserRules;

@Service
public class ContactServiceImpl implements ContactService {

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private ContactRules contactRules;

  @Autowired
  private UserRules userRules;


  @Override
  public void createAccount(Long userId, CreateContactRequestDto body) {
    User user = userRules.checkUserExistAndGet(userId);

    Contact createdContact = new Contact(
      body.getFullName(),
      body.getNote(),
      user
    );

    contactRepository.save(createdContact);
  }


  @Override
  public List<ListMyContactsResponseDto> listMyActiveContacts(Long userId) {
    List<Contact> dbContacts = contactRepository.findByUserIdAndIsRemovedFalseOrderByFullName(userId);
    List<ListMyContactsResponseDto> contacts = dbContacts.stream().map(contact -> new ListMyContactsResponseDto(
      contact.getId(),
      contact.getFullName(),
      contact.getNote()
    )).toList();

    return contacts;
  }

  @Override
  public void updateMyContact(Long userId, Long contactId, UpdateMyContactRequestDto body) {
    Contact dbContact = contactRules.checkUserContactExistAndGet(userId, contactId);

    dbContact.setFullName(body.getFullName());
    dbContact.setNote(body.getNote());
    contactRepository.save(dbContact);
    return;
  }

  @Override
  public void deleteContact(Long userId, Long contactId) {
    Contact dbContact = contactRules.checkUserContactExistAndGet(userId, contactId);
    dbContact.setRemoved(true);
    contactRepository.save(dbContact);
    return;
  }

  
}
