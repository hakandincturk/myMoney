package com.hakandincturk.security;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hakandincturk.core.handler.UnauthorizedResponseWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CSRF guard for cookie authentication: state-changing requests must come from an allowed origin
 * (the same list CORS uses). SameSite=Strict on the cookie is the first line; this is the second.
 */
@Component
public class OriginCheckFilter extends OncePerRequestFilter {

  private static final Set<String> STATE_CHANGING = Set.of("POST", "PUT", "PATCH", "DELETE");
  private static final Set<String> SESSIONLESS_PATHS = Set.of("/auth/login", "/auth/register");

  private final CorsConfigurationSource corsConfigurationSource;
  private final UnauthorizedResponseWriter responseWriter;

  public OriginCheckFilter(CorsConfigurationSource corsConfigurationSource, UnauthorizedResponseWriter responseWriter) {
    this.corsConfigurationSource = corsConfigurationSource;
    this.responseWriter = responseWriter;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return !STATE_CHANGING.contains(request.getMethod()) || SESSIONLESS_PATHS.contains(request.getServletPath());
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String origin = requestOrigin(request);
    CorsConfiguration cors = corsConfigurationSource.getCorsConfiguration(request);
    if (origin == null || cors == null || cors.checkOrigin(origin) == null) {
      responseWriter.write(response, HttpServletResponse.SC_FORBIDDEN, "İstek kaynağı doğrulanamadı");
      return;
    }
    filterChain.doFilter(request, response);
  }

  /** The Origin header, or scheme://host[:port] of the Referer when Origin is missing. */
  private String requestOrigin(HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin != null && !origin.isBlank() && !"null".equals(origin)) {
      return origin;
    }
    String referer = request.getHeader("Referer");
    if (referer == null || referer.isBlank()) {
      return null;
    }
    try {
      URI uri = URI.create(referer);
      if (uri.getScheme() == null || uri.getHost() == null) return null;
      return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
