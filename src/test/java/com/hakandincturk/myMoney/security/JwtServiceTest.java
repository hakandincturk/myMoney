package com.hakandincturk.myMoney.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.models.Users;
import com.hakandincturk.security.services.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

  private JwtService jwtService;
  private static final String TEST_SECRET;

  static {
    SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    TEST_SECRET = Base64.getEncoder().encodeToString(key.getEncoded());
  }

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    jwtService.SECRET_KEY = TEST_SECRET;
  }

  private Users createTestUser() {
    Users user = new Users();
    user.setId(1L);
    user.setEmail("test@test.com");
    user.setFullName("Test User");
    return user;
  }

  @Test
  @DisplayName("Token başarıyla oluşturulmalı")
  void generateToken_shouldCreateValidToken() {
    Users user = createTestUser();

    String token = jwtService.generateToken(user);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  @DisplayName("Token'dan email çıkarılmalı")
  void getEmailByToken_shouldReturnCorrectEmail() {
    Users user = createTestUser();
    String token = jwtService.generateToken(user);

    String email = jwtService.getEmailByToken(token);

    assertEquals("test@test.com", email);
  }

  @Test
  @DisplayName("Token'dan claims çıkarılmalı")
  void getClaims_shouldReturnClaims() {
    Users user = createTestUser();
    String token = jwtService.generateToken(user);

    Claims claims = jwtService.getClaims(token);

    assertNotNull(claims);
    assertEquals("test@test.com", claims.getSubject());
    assertEquals(1, claims.get("userId", Integer.class));
  }

  @Test
  @DisplayName("Geçerli token için tarih kontrolü true dönmeli")
  void isTokenDateValid_shouldReturnTrue_whenTokenNotExpired() {
    Users user = createTestUser();
    String token = jwtService.generateToken(user);

    boolean result = jwtService.isTokenDateValid(token);

    assertTrue(result);
  }

  @Test
  @DisplayName("Geçerli token ve kullanıcı için validation true dönmeli")
  void isTokenValid_shouldReturnTrue_whenTokenMatchesUser() {
    Users user = createTestUser();
    String token = jwtService.generateToken(user);

    boolean result = jwtService.isTokenValid(token, user);

    assertTrue(result);
  }

  @Test
  @DisplayName("Farklı kullanıcı için token validation false dönmeli")
  void isTokenValid_shouldReturnFalse_whenTokenDoesNotMatchUser() {
    Users user = createTestUser();
    String token = jwtService.generateToken(user);

    Users differentUser = new Users();
    differentUser.setId(2L);
    differentUser.setEmail("other@test.com");

    boolean result = jwtService.isTokenValid(token, differentUser);

    assertFalse(result);
  }

  @Test
  @DisplayName("exportToken ile custom claim çıkarılmalı")
  void exportToken_shouldExtractCustomClaim() {
    Users user = createTestUser();
    String token = jwtService.generateToken(user);

    String subject = jwtService.exportToken(token, Claims::getSubject);

    assertEquals("test@test.com", subject);
  }

  @Test
  @DisplayName("Key başarıyla oluşturulmalı")
  void getKey_shouldReturnValidKey() {
    assertNotNull(jwtService.getKey());
  }
}
