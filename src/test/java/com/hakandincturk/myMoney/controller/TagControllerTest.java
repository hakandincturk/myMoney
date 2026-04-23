package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.tag.request.FilterListUserTags;
import com.hakandincturk.dtos.tag.response.ListUserTagsDto;
import com.hakandincturk.dtos.tag.response.ListUserTagsWithTransactionCountDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.TagService;
import com.hakandincturk.webapi.controllers.impl.TagControllerImpl;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

  @InjectMocks
  private TagControllerImpl controller;

  @Mock
  private TagService tagService;

  private static final Long USER_ID = 1L;

  @BeforeEach
  void setUpSecurity() {
    JwtAuthentication auth = new JwtAuthentication("test@test.com", null, List.of(), USER_ID);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Etiket listeleme - başarılı")
  void listUserTags_shouldReturnSuccess() {
    Page<ListUserTagsDto> page = new PageImpl<>(List.of(new ListUserTagsDto()));
    when(tagService.listUserTags(eq(USER_ID), any())).thenReturn(page);

    FilterListUserTags body = new FilterListUserTags();
    ApiResponse<PagedResponse<ListUserTagsDto>> response = controller.listUserTags(body);

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Etiket listeleme - auth başarısız")
  void listUserTags_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass"));

    ApiResponse<PagedResponse<ListUserTagsDto>> response = controller.listUserTags(new FilterListUserTags());

    assertFalse(response.isType());
  }

  @Test
  @DisplayName("İşlem sayılı etiket listeleme - başarılı")
  void listUserTagsWithTransactionCount_shouldReturnSuccess() {
    Page<ListUserTagsWithTransactionCountDto> page = new PageImpl<>(List.of());
    when(tagService.listUserTagsWithTransactionCount(eq(USER_ID), any())).thenReturn(page);

    FilterListUserTags body = new FilterListUserTags();
    ApiResponse<PagedResponse<ListUserTagsWithTransactionCountDto>> response = controller.listUserTagsWithTransactionCount(body);

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Etiket silme - başarılı")
  void deleteTag_shouldReturnSuccess() {
    ApiResponse<?> response = controller.deleteTag(5L);

    assertTrue(response.isType());
    verify(tagService).deleteTag(USER_ID, 5L);
  }
}
