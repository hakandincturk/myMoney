package com.hakandincturk.core.specs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.transaction.request.TransactionFilterRequestDto;
import com.hakandincturk.models.Transaction;

import jakarta.persistence.criteria.Predicate;

public class TransactionSpecification {
  public static Specification<Transaction> filter(Long userId, TransactionFilterRequestDto body){
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Her zaman user ID filtreleme
      predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
      predicates.add(criteriaBuilder.equal(root.get("isRemoved"), false));

      if(body.getAccountIds() != null) {
        predicates.add(root.get("account").get("id").in(body.getAccountIds()));
      }

      if(body.getContactIds() != null){
        boolean isNullCheck = body.getContactIds().stream().anyMatch(contactId -> contactId == 0);

        if(body.getContactIds().size() == 1 && isNullCheck){
          predicates.add(criteriaBuilder.isNull(root.get("contact")));          
        }
        else if(body.getContactIds().size() > 0 && !isNullCheck){
          predicates.add(root.get("contact").get("id").in(body.getContactIds()));
        }
        else if(body.getContactIds().size() > 1 && isNullCheck){
          List<Long> contactIds = body.getContactIds().stream().filter(x -> x != 0).toList();
          predicates.add(
            criteriaBuilder.or(
              criteriaBuilder.isNull(root.get("contact")),
              root.get("contact").get("id").in(contactIds)
            )
          );
        }
      }

      if(body.getMinAmount() != null && body.getMaxAmount() != null){
        predicates.add(criteriaBuilder.between(root.get("totalAmount"), body.getMinAmount(), body.getMaxAmount()));
      }
      else if(body.getMinAmount() != null && body.getMaxAmount() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), body.getMinAmount()));
      }
      else if(body.getMinAmount() == null && body.getMaxAmount() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), body.getMaxAmount()));
      }

      if(body.getMinInstallmentCount() != null && body.getMaxInstallmentCount() != null) {
        predicates.add(criteriaBuilder.between(root.get("totalInstallment"), body.getMinInstallmentCount(), body.getMaxInstallmentCount()));
      }
      else if(body.getMinInstallmentCount() != null && body.getMaxInstallmentCount() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalInstallment"), body.getMinInstallmentCount()));
      }
      else if(body.getMinInstallmentCount() == null && body.getMaxInstallmentCount() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalInstallment"), body.getMaxInstallmentCount()));
      }

      if(body.getStartDate() != null && body.getEndDate() != null){
        predicates.add(criteriaBuilder.between(root.get("debtDate"), body.getStartDate(), body.getEndDate()));
      }
      else if(body.getStartDate() != null && body.getEndDate() == null){
        predicates.add(criteriaBuilder.greaterThan(root.get("debtDate"), body.getStartDate()));
      }
      else if(body.getStartDate() == null && body.getEndDate() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("debtDate"), body.getEndDate()));
      }

      if(body.getTypes() != null){
        predicates.add(root.get("type").in(body.getTypes()));
      }

      if(body.getName() != null && !body.getName().isEmpty()){
        predicates.add(criteriaBuilder.like(root.get("name"), "%" + body.getName() + "%"));
      }

      if(body.getStatuses() != null) {
        predicates.add(root.get("status").in(body.getStatuses()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
    
  }
}
