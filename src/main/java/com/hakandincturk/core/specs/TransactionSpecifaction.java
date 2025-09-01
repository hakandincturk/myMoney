package com.hakandincturk.core.specs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.transaction.request.TransactionFilterRequest;
import com.hakandincturk.models.Transaction;

import jakarta.persistence.criteria.Predicate;

public class TransactionSpecifaction {
  public static Specification<Transaction> filter(TransactionFilterRequest request, Long userId){
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Her zaman user ID filtreleme
      predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
      predicates.add(criteriaBuilder.equal(root.get("isRemoved"), false));

      if(request.getAccountIds() != null) {
        predicates.add(root.get("account").get("id").in(request.getAccountIds()));
      }

      if(request.getContactIds() != null){
        boolean isNullCheck = request.getContactIds().stream().anyMatch(contactId -> contactId == 0);;

        if(request.getContactIds().size() == 1 && isNullCheck){
          predicates.add(criteriaBuilder.isNull(root.get("contact")));          
        }
        else if(request.getContactIds().size() > 1 && !isNullCheck){
          predicates.add(root.get("contact").get("id").in(request.getContactIds()));
        }
        else if(request.getContactIds().size() > 1 && isNullCheck){
          predicates.add(
            criteriaBuilder.and(
              criteriaBuilder.isNull(root.get("contact")),
              root.get("contact").get("id").in(request.getContactIds())
            )
          );
        }
      }

      if(request.getMinAmount() != null && request.getMaxAmount() != null){
        predicates.add(criteriaBuilder.between(root.get("totalAmount"), request.getMinAmount(), request.getMaxAmount()));
      }
      else if(request.getMinAmount() != null && request.getMaxAmount() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), request.getMinAmount()));
      }
      else if(request.getMinAmount() == null && request.getMaxAmount() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), request.getMaxAmount()));
      }

      if(request.getMinInstallmentCount() != null && request.getMaxInstallmentCount() != null) {
        predicates.add(criteriaBuilder.between(root.get("totalInstallment"), request.getMinInstallmentCount(), request.getMaxInstallmentCount()));
      }
      else if(request.getMinInstallmentCount() != null && request.getMaxInstallmentCount() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalInstallment"), request.getMinInstallmentCount()));
      }
      else if(request.getMinInstallmentCount() == null && request.getMaxInstallmentCount() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalInstallment"), request.getMaxInstallmentCount()));
      }

      if(request.getStartDate() != null && request.getEndDate() != null){
        predicates.add(criteriaBuilder.between(root.get("debtDate"), request.getStartDate(), request.getEndDate()));
      }
      else if(request.getStartDate() != null && request.getEndDate() == null){
        predicates.add(criteriaBuilder.greaterThan(root.get("debtDate"), request.getStartDate()));
      }
      else if(request.getStartDate() == null && request.getEndDate() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("debtDate"), request.getEndDate()));
      }

      if(request.getTypes() != null){
        predicates.add(root.get("type").in(request.getTypes()));
      }

      if(request.getName() != null && !request.getName().isEmpty()){
        predicates.add(criteriaBuilder.like(root.get("name"), "%" + request.getName() + "%"));
      }


      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
    
  }
}
