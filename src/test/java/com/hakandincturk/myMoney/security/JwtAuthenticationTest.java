package com.hakandincturk.myMoney.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.security.JwtAuthentication;

class JwtAuthenticationTest {

  @Test
  @DisplayName("JwtAuthentication userId doğru döndürmeli")
  void getUserId_shouldReturnCorrectUserId() {
    Long userId = 42L;
    JwtAuthentication auth = new JwtAuthentication("test@test.com", null, List.of(), userId);

    assertEquals(42L, auth.getUserId());
  }

  @Test
  @DisplayName("JwtAuthentication principal doğru döndürmeli")
  void getPrincipal_shouldReturnEmail() {
    JwtAuthentication auth = new JwtAuthentication("test@test.com", null, List.of(), 1L);

    assertEquals("test@test.com", auth.getPrincipal());
  }

  @Test
  @DisplayName("JwtAuthentication authenticated olmalı")
  void isAuthenticated_shouldReturnTrue() {
    JwtAuthentication auth = new JwtAuthentication("test@test.com", null, List.of(), 1L);

    assertTrue(auth.isAuthenticated());
  }
}
