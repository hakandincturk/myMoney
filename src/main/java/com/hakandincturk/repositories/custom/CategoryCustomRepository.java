package com.hakandincturk.repositories.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.category.response.ListUserCategoriesWithTransactionCountDto;
import com.hakandincturk.models.Category;

public interface CategoryCustomRepository {
  Page<ListUserCategoriesWithTransactionCountDto> findWithTransactionCount(
    Specification<Category> specs, 
    Pageable page
  );
}
