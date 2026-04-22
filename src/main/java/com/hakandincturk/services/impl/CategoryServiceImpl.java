package com.hakandincturk.services.impl;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.specs.CategorySpecification;
import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;
import com.hakandincturk.dtos.category.response.ListUserCategoriesWithTransactionCountDto;
import com.hakandincturk.mappers.CategoryMapper;
import com.hakandincturk.models.Category;
import com.hakandincturk.models.TransactionCategory;
import com.hakandincturk.repositories.CategoryRepository;
import com.hakandincturk.repositories.TransactionCategoryRepository;
import com.hakandincturk.services.abstracts.CategoryService;
import com.hakandincturk.services.rules.CategoryRules;
import com.hakandincturk.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  
  private final CategoryRepository categoryRepository;
  private final TransactionCategoryRepository transactionCategoryRepository;
  private final CategoryMapper categoryMapper;
  private final CategoryRules categoryRules;

  @Override
  public Page<ListUserCategoriesDto> listUserCategories(Long userId, FilterListUserCategories body) {
    
    Pageable pageable = PaginationUtils.toPageable(body);
    Specification<Category> specs = CategorySpecification.filter(userId, body);
    Page<Category> categories = categoryRepository.findAll(specs, pageable);

    return categories.map(categoryMapper::toListUserCategoriesDto);
  }

  @Override
  public Page<ListUserCategoriesWithTransactionCountDto> listUserCategoriesWithTransactionCount(Long userId, FilterListUserCategories body) {
    Pageable pageable = PaginationUtils.toPageable(body);
    Specification<Category> specs = CategorySpecification.filter(userId, body);
    Page<ListUserCategoriesWithTransactionCountDto> result = categoryRepository.findWithTransactionCount(specs, pageable);
    return result;
  }

  @Override
  @Transactional
  public void deleteCategory(Long userId, Long categoryId) {
    Category category = categoryRules.checkUserCategoryExistAndGet(userId, categoryId);

    List<TransactionCategory> transactionCategories = transactionCategoryRepository.findAllByCategoryIdAndIsRemovedFalse(categoryId);
    if (transactionCategories != null && !transactionCategories.isEmpty()) {
      transactionCategories.forEach(transactionCategory -> transactionCategory.setRemoved(true));
      transactionCategoryRepository.saveAll(transactionCategories);
    }

    category.setRemoved(true);
    categoryRepository.save(category);
  }
}
