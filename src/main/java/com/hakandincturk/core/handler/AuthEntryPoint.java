package com.hakandincturk.core.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.hakandincturk.core.exception.UnauthorizedException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {
        System.out.println(1);
        throw new UnauthorizedException(authException.getMessage());
        // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
  }


  
}
