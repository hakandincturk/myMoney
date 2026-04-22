package com.hakandincturk.core.specs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.transaction.request.TagTransactionsFilterRequestDto;
import com.hakandincturk.models.TransactionTag;

import jakarta.persistence.criteria.Predicate;

public class TagTransactionFilterSpecification {
  public static Specification<TransactionTag> filter(Long userId, Long tagId, TagTransactionsFilterRequestDto body){
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(criteriaBuilder.equal(root.get("isRemoved"), false));
      predicates.add(criteriaBuilder.equal(root.get("transaction").get("isRemoved"), false));
      predicates.add(criteriaBuilder.equal(root.get("tag").get("user").get("id"), userId));
      predicates.add(criteriaBuilder.equal(root.get("tag").get("id"), tagId));
      predicates.add(criteriaBuilder.equal(root.get("tag").get("isRemoved"), false));

      // transaction name filter
      if(body.getTransactionName() != null && !body.getTransactionName().isBlank()){
        String name = body.getTransactionName().trim().toLowerCase(Locale.ROOT);
        predicates.add(criteriaBuilder.like(
          criteriaBuilder.lower(root.get("transaction").get("name")),
          "%" + name + "%"
        ));
      }

      // account filter
      if(body.getAccountIds() != null && !body.getAccountIds().isEmpty()) {
        predicates.add(root.get("transaction").get("account").get("id").in(body.getAccountIds()));
      }

      // total amount filter
      if(body.getMinAmount() != null && body.getMaxAmount() != null){
        predicates.add(criteriaBuilder.between(root.get("transaction").get("totalAmount"), body.getMinAmount(), body.getMaxAmount()));
      }
      else if(body.getMinAmount() != null && body.getMaxAmount() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transaction").get("totalAmount"), body.getMinAmount()));
      }
      else if(body.getMinAmount() == null && body.getMaxAmount() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transaction").get("totalAmount"), body.getMaxAmount()));
      }

      // Installment count filter
      if(body.getMinInstallmentCount() != null && body.getMaxInstallmentCount() != null) {
        predicates.add(criteriaBuilder.between(root.get("transaction").get("totalInstallment"), body.getMinInstallmentCount(), body.getMaxInstallmentCount()));
      }
      else if(body.getMinInstallmentCount() != null && body.getMaxInstallmentCount() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transaction").get("totalInstallment"), body.getMinInstallmentCount()));
      }
      else if(body.getMinInstallmentCount() == null && body.getMaxInstallmentCount() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transaction").get("totalInstallment"), body.getMaxInstallmentCount()));
      }

      if(body.getTypes() != null && !body.getTypes().isEmpty()){
        predicates.add(root.get("transaction").get("type").in(body.getTypes()));
      }

      if(body.getStatuses() != null && !body.getStatuses().isEmpty()) {
        predicates.add(root.get("transaction").get("status").in(body.getStatuses()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
