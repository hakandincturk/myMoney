package com.hakandincturk.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hakandincturk.core.handler.UnauthorizedResponseWriter;
import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;
import com.hakandincturk.security.session.SessionCookies;
import com.hakandincturk.services.abstracts.SessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Authenticates every request from the session cookie; unknown or ended sessions get a 401. */
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

  /** Paths reachable without a session (servlet paths, i.e. relative to the context path). */
  static final List<String> PUBLIC_PATHS = List.of(
    "/auth/login",
    "/auth/register",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/swagger-ui/index.html",
    "/v3/api-docs",
    "/v3/api-docs/**",
    "/v3/api-docs.yaml",
    "/webjars/**",
    "/swagger-resources/**",
    "/configuration/**"
  );

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  private final SessionService sessionService;
  private final SessionCookies sessionCookies;
  private final UnauthorizedResponseWriter unauthorizedResponseWriter;

  public SessionAuthenticationFilter(SessionService sessionService, SessionCookies sessionCookies,
      UnauthorizedResponseWriter unauthorizedResponseWriter) {
    this.sessionService = sessionService;
    this.sessionCookies = sessionCookies;
    this.unauthorizedResponseWriter = unauthorizedResponseWriter;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getServletPath();
    return PUBLIC_PATHS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    Optional<String> rawToken = sessionCookies.read(request);
    if (rawToken.isEmpty()) {
      unauthorizedResponseWriter.write(response, "Yetkisiz giriş. Lütfen tekrar giriş yapınız");
      return;
    }

    Optional<UserSession> session = sessionService.authenticate(rawToken.get());
    if (session.isEmpty()) {
      response.addHeader(HttpHeaders.SET_COOKIE, sessionCookies.clear().toString());
      unauthorizedResponseWriter.write(response, "Oturumunuzun süresi dolmuştur. Lütfen tekrar giriş yapınız");
      return;
    }

    Users user = session.get().getUser();
    UserSessionAuthentication authentication = new UserSessionAuthentication(
        user.getEmail(), user.getAuthorities(), user.getId(), session.get().getId());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }
}
