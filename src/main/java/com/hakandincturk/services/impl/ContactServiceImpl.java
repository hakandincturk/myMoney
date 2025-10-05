package com.hakandincturk.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.sort.ContactSortColumn;
import com.hakandincturk.core.specs.ContactSpecification;
import com.hakandincturk.dtos.contact.request.ContactFilterRequestDto;
import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;
import com.hakandincturk.mappers.ContactMapper;
import com.hakandincturk.models.Contact;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.ContactRepository;
import com.hakandincturk.services.abstracts.ContactService;
import com.hakandincturk.services.rules.ContactRules;
import com.hakandincturk.services.rules.UserRules;
import com.hakandincturk.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

  private final ContactRepository contactRepository;
  private final ContactRules contactRules;
  private final UserRules userRules;
  private final ContactMapper contactMapper;

  @Override
  public void createAccount(Long userId, CreateContactRequestDto body) {
    Users user = userRules.checkUserExistAndGet(userId);

    Contact createdContact = new Contact(
      body.getFullName(),
      body.getNote(),
      user
    );

    contactRepository.save(createdContact);
  }


  @Override
  public Page<ListMyContactsResponseDto> listMyActiveContacts(Long userId, ContactFilterRequestDto pageData) {

    Pageable pageable = PaginationUtils.toPageable(pageData, ContactSortColumn.class);
    Specification<Contact> specs = ContactSpecification.filter(userId, pageData);
    Page<Contact> dbContacts = contactRepository.findAll(specs, pageable);
    
    return dbContacts.map(contactMapper::toListMyContactsResponseDto);
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
