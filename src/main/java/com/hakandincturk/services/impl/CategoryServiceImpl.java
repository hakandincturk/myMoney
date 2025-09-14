package com.hakandincturk.services.impl;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.specs.CategorySpecification;
import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;
import com.hakandincturk.mappers.CategoryMapper;
import com.hakandincturk.models.Category;
import com.hakandincturk.repositories.CategoryRepository;
import com.hakandincturk.services.abstracts.CategoryService;
import com.hakandincturk.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  
  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public Page<ListUserCategoriesDto> listUserCategories(Long userId, FilterListUserCategories body) {
    
    Pageable pageable = PaginationUtils.toPageable(body);
    Specification<Category> specs = CategorySpecification.filter(userId, body);
    Page<Category> categories = categoryRepository.findAll(specs, pageable);

    return categories.map(categoryMapper::toListUserCategoriesDto);
  }
}
