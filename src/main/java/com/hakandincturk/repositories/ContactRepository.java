package com.hakandincturk.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

  Page<Contact> findByUserIdAndIsRemovedFalse(Long userId, Pageable pageData);
  Optional<Contact> findByIdAndUserIdAndIsRemovedFalse(Long contactId, Long userId);
  
}
