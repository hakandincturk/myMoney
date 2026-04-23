package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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

import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;
import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.dtos.account.request.CreateAccountRequestDto;
import com.hakandincturk.dtos.account.request.UpdateAccountRequestDto;
import com.hakandincturk.dtos.account.response.ListMyAccountsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.AccountService;
import com.hakandincturk.webapi.controllers.impl.AccountControllerImpl;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

  @InjectMocks
  private AccountControllerImpl controller;

  @Mock
  private AccountService accountService;

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
  @DisplayName("Hesap oluşturma - başarılı")
  void createAccount_shouldReturnSuccess() {
    CreateAccountRequestDto body = new CreateAccountRequestDto("Banka", AccountTypes.BANK, CurrencyTypes.TL, BigDecimal.valueOf(5000));

    ApiResponse<?> response = controller.createAccount(body);

    assertTrue(response.isType());
    verify(accountService).createAccount(body, USER_ID);
  }

  @Test
  @DisplayName("Hesap oluşturma - auth başarısız")
  void createAccount_shouldReturnError_whenNotJwtAuth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass"));

    CreateAccountRequestDto body = new CreateAccountRequestDto("Test", AccountTypes.BANK, CurrencyTypes.TL, BigDecimal.ZERO);
    ApiResponse<?> response = controller.createAccount(body);

    assertFalse(response.isType());
  }

  @Test
  @DisplayName("Aktif hesapları listeleme - başarılı")
  void listMyActiveAccounts_shouldReturnSuccess() {
    ListMyAccountsResponseDto dto = new ListMyAccountsResponseDto();
    dto.setId(1L);
    Page<ListMyAccountsResponseDto> page = new PageImpl<>(List.of(dto));

    when(accountService.listMyActiveAccounts(eq(USER_ID), any())).thenReturn(page);

    SortablePageRequest pageData = new SortablePageRequest();
    ApiResponse<PagedResponse<ListMyAccountsResponseDto>> response = controller.listMyActiveAccounts(pageData);

    assertTrue(response.isType());
    assertEquals(1, response.getData().getContent().size());
  }

  @Test
  @DisplayName("Hesap güncelleme - başarılı")
  void updateMyAccount_shouldReturnSuccess() {
    UpdateAccountRequestDto body = new UpdateAccountRequestDto("Yeni İsim", BigDecimal.valueOf(10000));

    ApiResponse<?> response = controller.updateMyAccount(5L, body);

    assertTrue(response.isType());
    verify(accountService).updateMyAccount(USER_ID, 5L, body);
  }
}
