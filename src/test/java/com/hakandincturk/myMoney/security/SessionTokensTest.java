package com.hakandincturk.myMoney.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.security.session.SessionTokens;

class SessionTokensTest {

  @Test
  @DisplayName("Yeni token 32 baytlık base64url değer olmalı ve her seferinde farklı olmalı")
  void newRawToken_is32BytesBase64UrlAndUnique() {
    String a = SessionTokens.newRawToken();
    String b = SessionTokens.newRawToken();

    assertEquals(32, Base64.getUrlDecoder().decode(a).length);
    assertFalse(a.contains("="));
    assertNotEquals(a, b);
  }

  @Test
  @DisplayName("Hash SHA-256 küçük harf hex olmalı")
  void hash_isLowercaseSha256Hex() {
    assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", SessionTokens.hash("abc"));
  }
}
