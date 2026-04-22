package com.hakandincturk.webapi.controllers.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.category.request.FilterListUserCategories;
import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;
import com.hakandincturk.dtos.category.response.ListUserCategoriesWithTransactionCountDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.CategoryService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.CategoryController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/category")
@Tag(name = "Category", description = "Kategori işlemleri")
public class CategoryControllerImpl extends BaseController implements CategoryController {

  private final CategoryService categoryService;

  @Override
  @GetMapping(value = "/my/active")
  @Operation(summary = "Get user categories", description = "Kullanıcıya ait kategorileri getirir")
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


  @Override
  @GetMapping(value = "/my")
  @Operation(summary = "Get user categories with transaction count", description = "Kullanıcıya ait kategorileri gelir/gider sayıları ile beraber getirir")
  public ApiResponse<PagedResponse<ListUserCategoriesWithTransactionCountDto>> listUserCategoriesWithTransactionCount(FilterListUserCategories body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return successPaged("Kullanıcı kategorileri getirildi", categoryService.listUserCategoriesWithTransactionCount(userId, body));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @DeleteMapping(value = "/my/{categoryId}")
  @Operation(summary = "Delete category", description = "Kullanıcıya ait kategoriyi siler")
  public ApiResponse<?> deleteCategory(@PathVariable(name = "categoryId") Long categoryId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      categoryService.deleteCategory(userId, categoryId);
      return success("Kategori başarıyla silindi", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
}
