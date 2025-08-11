package com.hakandincturk.services.rules;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Contact;
import com.hakandincturk.repositories.ContactRepository;

@Service
public class ContactRules {

  @Autowired
  private ContactRepository contactRepository;
  
  public Contact checkUserContactExistAndGet(Long userId, Long contactId){
    Optional<Contact> dbContact = contactRepository.findByIdAndUserIdAndIsRemovedFalse(contactId, userId);
    if(dbContact.isEmpty()){
      throw new NotFoundException("Kisi bulunamadı");
    }

    return dbContact.get();
  }
}
