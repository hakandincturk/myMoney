package com.hakandincturk.services.abstracts;

import org.springframework.data.domain.Page;

import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;
import com.hakandincturk.dtos.category.response.ListUserCategoriesWithTransactionCountDto;

public interface CategoryService {
  public Page<ListUserCategoriesDto> listUserCategories(Long userId, FilterListUserCategories body);
  public Page<ListUserCategoriesWithTransactionCountDto> listUserCategoriesWithTransactionCount(Long userId, FilterListUserCategories body);
}
