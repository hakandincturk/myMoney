package com.hakandincturk.core.specs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.models.Category;

import jakarta.persistence.criteria.Predicate;

public class CategorySpecification {
  public static Specification<Category> filter(Long userId, FilterListUserCategories body){
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(criteriaBuilder.equal(root.get("isRemoved"), false));
      predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

      if(body.getName() != null && !body.getName().isEmpty()){
        predicates.add(criteriaBuilder.like(root.get("name"), "%" + body.getName() + "%"));
      }

      if(body.getCreatedStartDate() != null && body.getCreatedEndDate() != null){
        predicates.add(criteriaBuilder.between(root.get("createdAt"), body.getCreatedStartDate(), body.getCreatedEndDate()));
      }
      else if(body.getCreatedStartDate() != null && body.getCreatedEndDate() == null){
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), body.getCreatedStartDate()));
      }
      else if(body.getCreatedStartDate() == null && body.getCreatedEndDate() != null){
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), body.getCreatedEndDate()));
      }
      
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
