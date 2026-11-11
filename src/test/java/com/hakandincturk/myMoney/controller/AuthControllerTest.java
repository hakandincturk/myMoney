package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.dtos.auth.response.MeResponseDto;
import com.hakandincturk.dtos.auth.response.RevokeSessionsResponseDto;
import com.hakandincturk.security.UserSessionAuthentication;
import com.hakandincturk.security.session.ClientInfoResolver;
import com.hakandincturk.security.session.SessionCookies;
import com.hakandincturk.security.session.SessionProperties;
import com.hakandincturk.services.abstracts.AuthService;
import com.hakandincturk.services.abstracts.AuthService.LoginResult;
import com.hakandincturk.webapi.controllers.impl.AuthControllerImpl;

import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private static final Long USER_ID = 1L;
  private static final Long SESSION_ID = 10L;

  @Mock
  private AuthService authService;

  private AuthControllerImpl controller;

  @BeforeEach
  void setUp() {
    controller = new AuthControllerImpl(authService, new SessionCookies(new SessionProperties()), new ClientInfoResolver());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticate() {
    SecurityContextHolder.getContext().setAuthentication(
        new UserSessionAuthentication("test@test.com", List.of(), USER_ID, SESSION_ID));
  }

  @Test
  @DisplayName("Giriş HttpOnly + SameSite=Strict cookie koymalı, gövdede token olmamalı")
  void login_setsHttpOnlyStrictCookie_andReturnsNoToken() {
    LoginRequestDto body = new LoginRequestDto("test@test.com", "password");
    when(authService.login(eq(body), any(), eq(Optional.empty())))
        .thenReturn(new LoginResult("raw-token", new LoginResponseDto("Test User", "test@test.com")));
    MockHttpServletResponse response = new MockHttpServletResponse();

    ApiResponse<LoginResponseDto> result = controller.login(body, new MockHttpServletRequest(), response);

    assertTrue(result.isType());
    assertEquals("Test User", result.getData().getFullName());
    String cookie = response.getHeader("Set-Cookie");
    assertNotNull(cookie);
    assertTrue(cookie.startsWith("mm_session=raw-token;"), cookie);
    assertTrue(cookie.contains("HttpOnly"), cookie);
    assertTrue(cookie.contains("Secure"), cookie);
    assertTrue(cookie.contains("SameSite=Strict"), cookie);
    assertTrue(cookie.contains("Max-Age=2592000"), cookie);
    assertTrue(cookie.contains("Path=/"), cookie);
  }

  @Test
  @DisplayName("Giriş mevcut cookie'yi servise iletmeli")
  void login_passesExistingCookieToService() {
    LoginRequestDto body = new LoginRequestDto("test@test.com", "password");
    when(authService.login(eq(body), any(), eq(Optional.of("old-raw"))))
        .thenReturn(new LoginResult("new-raw", new LoginResponseDto("Test User", "test@test.com")));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("mm_session", "old-raw"));

    controller.login(body, request, new MockHttpServletResponse());

    verify(authService).login(eq(body), any(), eq(Optional.of("old-raw")));
  }

  @Test
  @DisplayName("Çıkış mevcut oturumu kapatmalı ve cookie'yi silmeli")
  void logout_revokesCurrent_andClearsCookie() {
    authenticate();
    MockHttpServletResponse response = new MockHttpServletResponse();

    ApiResponse<?> result = controller.logout(response);

    assertTrue(result.isType());
    verify(authService).logout(SESSION_ID);
    assertTrue(response.getHeader("Set-Cookie").contains("Max-Age=0"));
  }

  @Test
  @DisplayName("Me mevcut kullanıcıyı döndürmeli")
  void me_returnsCurrentUser() {
    authenticate();
    when(authService.me(USER_ID)).thenReturn(new MeResponseDto(USER_ID, "Test User", "test@test.com"));

    assertEquals("test@test.com", controller.me().getData().getEmail());
  }

  @Test
  @DisplayName("Oturum listesi mevcut oturum id'si ile istenmeli")
  void sessions_passesCurrentSessionId() {
    authenticate();
    when(authService.listSessions(USER_ID, SESSION_ID)).thenReturn(List.of());

    assertTrue(controller.sessions().isType());
    verify(authService).listSessions(USER_ID, SESSION_ID);
  }

  @Test
  @DisplayName("Başka bir oturumu kapatmak cookie'ye dokunmamalı")
  void revokeOther_doesNotTouchCookie() {
    authenticate();
    MockHttpServletResponse response = new MockHttpServletResponse();

    controller.revokeSession(22L, response);

    verify(authService).revokeSession(USER_ID, 22L);
    assertNull(response.getHeader("Set-Cookie"));
  }

  @Test
  @DisplayName("Kendi oturumunu listeden kapatmak cookie'yi de silmeli")
  void revokeOwnCurrent_clearsCookie() {
    authenticate();
    MockHttpServletResponse response = new MockHttpServletResponse();

    controller.revokeSession(SESSION_ID, response);

    verify(authService).revokeSession(USER_ID, SESSION_ID);
    assertTrue(response.getHeader("Set-Cookie").contains("Max-Age=0"));
  }

  @Test
  @DisplayName("Diğer oturumları kapatma sayıyı döndürmeli")
  void revokeOthers_returnsCount() {
    authenticate();
    when(authService.revokeOtherSessions(USER_ID, SESSION_ID)).thenReturn(new RevokeSessionsResponseDto(2));

    assertEquals(2, controller.revokeOtherSessions().getData().getRevokedCount());
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
