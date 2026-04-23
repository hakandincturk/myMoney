package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.services.abstracts.AuthService;
import com.hakandincturk.webapi.controllers.impl.AuthControllerImpl;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @InjectMocks
  private AuthControllerImpl controller;

  @Mock
  private AuthService authService;

  @Test
  @DisplayName("Giriş - başarılı token döndürmeli")
  void login_shouldReturnTokenOnSuccess() {
    LoginRequestDto body = new LoginRequestDto("test@test.com", "password");
    LoginResponseDto loginResponse = new LoginResponseDto("jwt-token");

    when(authService.login(body)).thenReturn(loginResponse);

    ApiResponse<LoginResponseDto> response = controller.login(body);

    assertTrue(response.isType());
    assertEquals("jwt-token", response.getData().getToken());
  }

  @Test
  @DisplayName("Kayıt - başarılı")
  void register_shouldReturnSuccess() {
    RegisterRequestDto body = new RegisterRequestDto("Test User", "test@test.com", "password", "5551234567");

    ApiResponse<?> response = controller.register(body);

    assertTrue(response.isType());
    verify(authService).register(body);
  }
}
