package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

  List<Contact> findByUserIdAndIsRemovedFalseOrderByFullName(Long userId);
  Optional<Contact> findByIdAndUserIdAndIsRemovedFalse(Long contactId, Long userId);
  
}
