package com.hakandincturk.webapi.controllers.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.CategoryService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.CategoryController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/category")
@Tag(name = "Category", description = "Kategori işlemleri")
public class CategoryControllerImpl extends BaseController implements CategoryController {

  private final CategoryService categoryService;

  @Override
  @GetMapping(value = "/my/active")
  public ApiResponse<PagedResponse<ListUserCategoriesDto>> listUserCategories(@ModelAttribute FilterListUserCategories body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return successPaged("Kategoriler getirildi", categoryService.listUserCategories(userId, body));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
}
