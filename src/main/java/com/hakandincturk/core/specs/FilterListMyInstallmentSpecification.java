package com.hakandincturk.core.specs;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.installment.request.FilterListMyInstallmentRequestDto;
import com.hakandincturk.models.Installment;

import jakarta.persistence.criteria.Predicate;

public class FilterListMyInstallmentSpecification {
  public static Specification<Installment> filter(Long userId, FilterListMyInstallmentRequestDto body){
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(criteriaBuilder.equal(root.get("isRemoved"), false));
      predicates.add(criteriaBuilder.equal(root.get("transaction").get("user").get("id"), userId));

      LocalDate debtStartDate = LocalDate.of(body.getYear(), body.getMonth(), 1);
      LocalDate debtEndDate = YearMonth.of(body.getYear(), body.getMonth()).atEndOfMonth();
      predicates.add(criteriaBuilder.between(root.get("debtDate"), debtStartDate, debtEndDate));

      if(body.getTransactionName() != null && !body.getTransactionName().isEmpty()){
        predicates.add(criteriaBuilder.like(
          criteriaBuilder.lower(root.get("transaction").get("name")),
          "%" + body.getTransactionName().toLowerCase() + "%"
        ));
      }

      if(body.getDescription() != null && !body.getDescription().isEmpty()){
        predicates.add(criteriaBuilder.like(
          criteriaBuilder.lower(root.get("description")),
          "%" + body.getDescription().toLowerCase() + "%"
        ));
      }

      if(body.getIsPaid() != null && body.getIsPaid().size() > 0){
        predicates.add(root.get("isPaid").in(body.getIsPaid()));
      }

      if(body.getMinTotalAmount() != null && body.getMaxTotalAmount() != null){
        predicates.add(criteriaBuilder.between(root.get("amount"), body.getMinTotalAmount(), body.getMaxTotalAmount()));
      }
      else if(body.getMinTotalAmount() != null && body.getMaxTotalAmount() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), body.getMinTotalAmount()));
      }
      else if(body.getMinTotalAmount() == null && body.getMaxTotalAmount() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), body.getMaxTotalAmount()));
      }

      if(body.getPaidStartDate() != null && body.getPaidEndDate() != null){
        predicates.add(criteriaBuilder.between(root.get("paidDate"), body.getPaidStartDate(), body.getPaidEndDate()));
      }
      else if(body.getPaidStartDate() != null && body.getPaidEndDate() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("paidDate"), body.getPaidStartDate()));
      }
      else if(body.getPaidStartDate() == null && body.getPaidEndDate() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("paidDate"), body.getPaidEndDate()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
