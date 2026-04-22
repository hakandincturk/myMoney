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
import com.hakandincturk.dtos.tag.request.FilterListUserTags;
import com.hakandincturk.dtos.tag.response.ListUserTagsDto;
import com.hakandincturk.dtos.tag.response.ListUserTagsWithTransactionCountDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.TagService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.abstracts.TagController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/tag")
@Tag(name = "Tag", description = "Etiket işlemleri")
public class TagControllerImpl extends BaseController implements TagController {

  private final TagService tagService;

  @Override
  @GetMapping(value = "/my/active")
  @Operation(summary = "Get user tags", description = "Kullanıcıya ait etiketleri getirir")
  public ApiResponse<PagedResponse<ListUserTagsDto>> listUserTags(@ModelAttribute FilterListUserTags body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return successPaged("Etiketler getirildi", tagService.listUserTags(userId, body));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }


  @Override
  @GetMapping(value = "/my")
  @Operation(summary = "Get user tags with transaction count", description = "Kullanıcıya ait etiketleri gelir/gider sayıları ile beraber getirir")
  public ApiResponse<PagedResponse<ListUserTagsWithTransactionCountDto>> listUserTagsWithTransactionCount(FilterListUserTags body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return successPaged("Kullanıcı etiketleri getirildi", tagService.listUserTagsWithTransactionCount(userId, body));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @DeleteMapping(value = "/my/{tagId}")
  @Operation(summary = "Delete tag", description = "Kullanıcıya ait etiketi siler")
  public ApiResponse<?> deleteTag(@PathVariable(name = "tagId") Long tagId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      tagService.deleteTag(userId, tagId);
      return success("Etiket başarıyla silindi", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
}
