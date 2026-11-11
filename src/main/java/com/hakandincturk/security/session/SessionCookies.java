package com.hakandincturk.security.session;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/** Builds and reads the HttpOnly session cookie. */
@Component
public class SessionCookies {

  private final SessionProperties properties;

  public SessionCookies(SessionProperties properties) {
    this.properties = properties;
  }

  public ResponseCookie issue(String rawToken) {
    return base(rawToken).maxAge(properties.getAbsoluteTimeout()).build();
  }

  public ResponseCookie clear() {
    return base("").maxAge(0).build();
  }

  public Optional<String> read(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return Arrays.stream(cookies)
        .filter(c -> properties.getCookieName().equals(c.getName()))
        .map(Cookie::getValue)
        .filter(v -> v != null && !v.isBlank())
        .findFirst();
  }

  private ResponseCookie.ResponseCookieBuilder base(String value) {
    return ResponseCookie.from(properties.getCookieName(), value)
        .httpOnly(true)
        .secure(properties.isCookieSecure())
        .sameSite("Strict")
        .path("/");
  }
}
