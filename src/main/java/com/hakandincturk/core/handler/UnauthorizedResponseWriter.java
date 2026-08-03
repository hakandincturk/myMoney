package com.hakandincturk.core.handler;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hakandincturk.core.payload.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Authentication hataları controller advice'ın dışında oluştuğu için response'u kendisi yazar.
 * Spring'in yönettiği ObjectMapper kullanılır; aksi halde tarih alanları global Jackson
 * konfigürasyonundan bağımsız serialize edilir ve aynı API iki farklı timestamp formatı üretir.
 */
@Component
@RequiredArgsConstructor
public class UnauthorizedResponseWriter {

  private final ObjectMapper objectMapper;

  public void write(HttpServletResponse response, String message) throws IOException {
    ApiResponse<Object> api = new ApiResponse<>(false, message, LocalDateTime.now(), null);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    objectMapper.writeValue(response.getWriter(), api);
    response.getWriter().flush();
  }

}
