package com.hakandincturk.myMoney.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.security.UserSessionAuthentication;

class UserSessionAuthenticationTest {

  @Test
  @DisplayName("UserSessionAuthentication userId doğru döndürmeli")
  void getUserId_shouldReturnCorrectUserId() {
    Long userId = 42L;
    UserSessionAuthentication auth = new UserSessionAuthentication("test@test.com", List.of(), userId, null);

    assertEquals(42L, auth.getUserId());
  }

  @Test
  @DisplayName("UserSessionAuthentication principal doğru döndürmeli")
  void getPrincipal_shouldReturnEmail() {
    UserSessionAuthentication auth = new UserSessionAuthentication("test@test.com", List.of(), 1L, null);

    assertEquals("test@test.com", auth.getPrincipal());
  }

  @Test
  @DisplayName("UserSessionAuthentication authenticated olmalı")
  void isAuthenticated_shouldReturnTrue() {
    UserSessionAuthentication auth = new UserSessionAuthentication("test@test.com", List.of(), 1L, null);

    assertTrue(auth.isAuthenticated());
  }

  @Test
  @DisplayName("UserSessionAuthentication sessionId doğru döndürmeli")
  void getSessionId_shouldReturnCorrectSessionId() {
    UserSessionAuthentication auth = new UserSessionAuthentication("test@test.com", List.of(), 1L, 9L);

    assertEquals(9L, auth.getSessionId());
  }
}
