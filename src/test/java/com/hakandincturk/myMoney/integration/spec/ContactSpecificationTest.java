package com.hakandincturk.myMoney.integration.spec;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.core.specs.ContactSpecification;
import com.hakandincturk.dtos.contact.request.ContactFilterRequestDto;
import com.hakandincturk.models.Contact;

class ContactSpecificationTest extends BaseSpecificationTest {

  @Test
  @DisplayName("Contact filter - isim ile filtreleme")
  void shouldFilterByFullName() {
    createContact("Ali Veli");
    createContact("Ayşe Fatma");

    ContactFilterRequestDto filter = new ContactFilterRequestDto();
    filter.setFullName("ali");

    Specification<Contact> spec = ContactSpecification.filter(testUser.getId(), filter);
    Page<Contact> result = contactRepository.findAll(spec, PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals("Ali Veli", result.getContent().get(0).getFullName());
  }

  @Test
  @DisplayName("Contact filter - not ile filtreleme")
  void shouldFilterByNote() {
    Contact c1 = createContact("Ali");
    c1.setNote("Arkadaşım");
    contactRepository.save(c1);

    Contact c2 = createContact("Veli");
    c2.setNote("İş arkadaşı");
    contactRepository.save(c2);

    ContactFilterRequestDto filter = new ContactFilterRequestDto();
    filter.setNote("arkadaş");

    Specification<Contact> spec = ContactSpecification.filter(testUser.getId(), filter);
    Page<Contact> result = contactRepository.findAll(spec, PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  @DisplayName("Contact filter - boş filtre tüm aktif kişileri getirmeli")
  void emptyFilter_shouldReturnAll() {
    createContact("Ali");
    createContact("Veli");

    ContactFilterRequestDto filter = new ContactFilterRequestDto();

    Specification<Contact> spec = ContactSpecification.filter(testUser.getId(), filter);
    Page<Contact> result = contactRepository.findAll(spec, PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  @DisplayName("Contact filter - silinmiş kişileri getirmemeli")
  void shouldNotReturnRemovedContacts() {
    createContact("Aktif");
    Contact removed = createContact("Silinmiş");
    removed.setRemoved(true);
    contactRepository.save(removed);

    ContactFilterRequestDto filter = new ContactFilterRequestDto();

    Specification<Contact> spec = ContactSpecification.filter(testUser.getId(), filter);
    Page<Contact> result = contactRepository.findAll(spec, PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
  }
}
