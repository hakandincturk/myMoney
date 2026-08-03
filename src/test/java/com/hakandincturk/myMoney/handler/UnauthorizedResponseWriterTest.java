package com.hakandincturk.myMoney.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hakandincturk.core.handler.UnauthorizedResponseWriter;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UnauthorizedResponseWriterTest {

  // Spring Boot'un uygulama genelinde ürettiği ObjectMapper ile aynı konfigürasyon
  private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
      .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .build();

  private final UnauthorizedResponseWriter writer = new UnauthorizedResponseWriter(objectMapper);

  @Test
  @DisplayName("Unauthorized response doğru format ve status ile yazılmalı")
  void write_shouldSetCorrectStatusAndContentType() throws IOException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    writer.write(response, "Yetkisiz giriş");

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertEquals("application/json;charset=UTF-8", response.getContentType());
    String content = response.getContentAsString();
    assertTrue(content.contains("\"type\":false"));
    assertTrue(content.contains("Yetkisiz giriş"));
  }

  @Test
  @DisplayName("Response body JSON formatında olmalı")
  void write_shouldWriteValidJson() throws IOException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    writer.write(response, "Test mesajı");

    String content = response.getContentAsString();
    assertTrue(content.startsWith("{"));
    assertTrue(content.endsWith("}"));
    assertTrue(content.contains("\"message\":\"Test mesajı\""));
  }

  @Test
  @DisplayName("Timestamp ISO string olarak yazılmalı, sayı dizisi olarak değil")
  void write_shouldSerializeTimestampAsIsoString() throws IOException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    writer.write(response, "Yetkisiz giriş");

    String content = response.getContentAsString();
    assertTrue(content.matches(".*\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"), content);
    assertFalse(content.contains("\"timestamp\":["), content);
  }
}
