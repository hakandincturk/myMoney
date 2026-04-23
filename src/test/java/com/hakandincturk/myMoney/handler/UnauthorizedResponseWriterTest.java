package com.hakandincturk.myMoney.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import com.hakandincturk.core.handler.UnauthorizedResponseWriter;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UnauthorizedResponseWriterTest {

  @Test
  @DisplayName("Unauthorized response doğru format ve status ile yazılmalı")
  void write_shouldSetCorrectStatusAndContentType() throws IOException {
    MockHttpServletResponse response = new MockHttpServletResponse();

    UnauthorizedResponseWriter.write(response, "Yetkisiz giriş");

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

    UnauthorizedResponseWriter.write(response, "Test mesajı");

    String content = response.getContentAsString();
    assertTrue(content.startsWith("{"));
    assertTrue(content.endsWith("}"));
    assertTrue(content.contains("\"message\":\"Test mesajı\""));
  }
}
