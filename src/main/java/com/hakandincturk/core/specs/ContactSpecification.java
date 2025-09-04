package com.hakandincturk.core.specs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.contact.request.ContactFilterRequestDto;
import com.hakandincturk.models.Contact;

import jakarta.persistence.criteria.Predicate;

public class ContactSpecification {
  public static Specification<Contact> filter(Long userId, ContactFilterRequestDto body){
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // userIdyi her zaman filtrele
      predicates.add(criteriaBuilder.equal(root.get("isRemoved"), false));
      predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

      if(body.getFullName() != null && !body.getFullName().isEmpty()){
        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + body.getFullName().toLowerCase() + "%"));
      }

      if(body.getNote() != null && !body.getNote().isEmpty()){
        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), "%" + body.getNote().toLowerCase() + "%"));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
