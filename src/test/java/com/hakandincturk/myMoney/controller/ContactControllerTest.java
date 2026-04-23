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
import com.hakandincturk.dtos.contact.request.ContactFilterRequestDto;
import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.ContactService;
import com.hakandincturk.webapi.controllers.impl.ContactControllerImpl;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

  @InjectMocks
  private ContactControllerImpl controller;

  @Mock
  private ContactService contactService;

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
  @DisplayName("Kişi oluşturma - başarılı")
  void createContact_shouldReturnSuccess() {
    CreateContactRequestDto body = new CreateContactRequestDto("Ali Veli", "Not");

    ApiResponse<?> response = controller.createContact(body);

    assertTrue(response.isType());
    verify(contactService).createAccount(USER_ID, body);
  }

  @Test
  @DisplayName("Kişi oluşturma - auth başarısız")
  void createContact_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass"));

    ApiResponse<?> response = controller.createContact(new CreateContactRequestDto("Test", ""));

    assertFalse(response.isType());
  }

  @Test
  @DisplayName("Aktif kişileri listeleme - başarılı")
  void listMyActiveContacts_shouldReturnSuccess() {
    Page<ListMyContactsResponseDto> page = new PageImpl<>(List.of(new ListMyContactsResponseDto()));
    when(contactService.listMyActiveContacts(eq(USER_ID), any())).thenReturn(page);

    ContactFilterRequestDto pageData = new ContactFilterRequestDto();
    ApiResponse<PagedResponse<ListMyContactsResponseDto>> response = controller.listMyActiveContacts(pageData);

    assertTrue(response.isType());
  }

  @Test
  @DisplayName("Kişi güncelleme - başarılı")
  void updateMyContact_shouldReturnSuccess() {
    UpdateMyContactRequestDto body = new UpdateMyContactRequestDto("Yeni İsim", "Yeni Not");

    ApiResponse<?> response = controller.updateMyAccount(5L, body);

    assertTrue(response.isType());
    verify(contactService).updateMyContact(USER_ID, 5L, body);
  }

  @Test
  @DisplayName("Kişi silme - başarılı")
  void deleteContact_shouldReturnSuccess() {
    ApiResponse<?> response = controller.deleteContact(5L);

    assertTrue(response.isType());
    verify(contactService).deleteContact(USER_ID, 5L);
  }
}
