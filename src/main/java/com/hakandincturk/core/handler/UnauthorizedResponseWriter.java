package com.hakandincturk.core.handler;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hakandincturk.core.payload.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

public class UnauthorizedResponseWriter {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static void write(HttpServletResponse response, String message) throws IOException {
    ApiResponse<Object> api = new ApiResponse<>(false, message, LocalDateTime.now(), null);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    MAPPER.writeValue(response.getWriter(), api);
    response.getWriter().flush();
  }

}
