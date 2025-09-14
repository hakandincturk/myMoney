package com.hakandincturk.services.abstracts;

import org.springframework.data.domain.Page;

import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;

public interface CategoryService {
  public Page<ListUserCategoriesDto> listUserCategories(Long userId, FilterListUserCategories body);
}
