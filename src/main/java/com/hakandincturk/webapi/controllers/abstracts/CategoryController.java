package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;

public interface CategoryController {
  ApiResponse<PagedResponse<ListUserCategoriesDto>> listUserCategories(FilterListUserCategories body);
}
