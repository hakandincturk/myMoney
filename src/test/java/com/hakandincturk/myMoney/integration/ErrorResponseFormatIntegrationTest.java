package com.hakandincturk.myMoney.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Hata response'larının da başarılı response'larla aynı JSON sözleşmesini kullandığını doğrular.
 * Aynı API'nin iki farklı tarih formatı üretmesi istemci tarafında parse hatasına yol açar.
 */
@AutoConfigureMockMvc
class ErrorResponseFormatIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Geçersiz token ile istek 401 dönmeli ve timestamp ISO string olmalı")
  void unauthorizedResponse_shouldUseIsoStringTimestamp() throws Exception {
    String body = mockMvc.perform(MockMvcRequestBuilders.get("/report/summary?year=2026&month=6")
            .servletPath("/report/summary")
            .header("Authorization", "Bearer gecersiz.token.degeri"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertTrue(body.matches(".*\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"), body);
    assertFalse(body.contains("\"timestamp\":["), body);
    assertTrue(body.contains("\"type\":false"), body);
  }

  @Test
  @DisplayName("Authorization header olmadan istek 401 dönmeli ve timestamp ISO string olmalı")
  void missingTokenResponse_shouldUseIsoStringTimestamp() throws Exception {
    String body = mockMvc.perform(MockMvcRequestBuilders.get("/report/summary?year=2026&month=6")
            .servletPath("/report/summary"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertTrue(body.matches(".*\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"), body);
    assertFalse(body.contains("\"timestamp\":["), body);
  }
}
