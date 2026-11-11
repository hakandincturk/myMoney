package com.hakandincturk.myMoney.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hakandincturk.core.handler.UnauthorizedResponseWriter;
import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;
import com.hakandincturk.security.SessionAuthenticationFilter;
import com.hakandincturk.security.UserSessionAuthentication;
import com.hakandincturk.security.session.SessionCookies;
import com.hakandincturk.security.session.SessionProperties;
import com.hakandincturk.services.abstracts.SessionService;

import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class SessionAuthenticationFilterTest {

  @Mock
  private SessionService sessionService;

  private SessionAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    UnauthorizedResponseWriter writer = new UnauthorizedResponseWriter(new ObjectMapper().findAndRegisterModules());
    filter = new SessionAuthenticationFilter(sessionService, new SessionCookies(new SessionProperties()), writer);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Cookie yoksa 401 ve 'Yetkisiz giriş' mesajı dönmeli")
  void noCookie_returns401WithYetkisizMessage() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request("/report/summary"), response, chain);

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("Yetkisiz giriş. Lütfen tekrar giriş yapınız"));
    assertNull(chain.getRequest(), "chain must not continue");
  }

  @Test
  @DisplayName("Bilinmeyen token 401 dönmeli ve cookie'yi silmeli")
  void unknownToken_returns401AndClearsCookie() throws Exception {
    when(sessionService.authenticate("bad")).thenReturn(Optional.empty());
    MockHttpServletRequest request = request("/report/summary");
    request.setCookies(new Cookie("mm_session", "bad"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("Oturumunuzun süresi dolmuştur. Lütfen tekrar giriş yapınız"));
    String setCookie = response.getHeader("Set-Cookie");
    assertNotNull(setCookie);
    assertTrue(setCookie.startsWith("mm_session=;"), setCookie);
    assertTrue(setCookie.contains("Max-Age=0"), setCookie);
  }

  @Test
  @DisplayName("Geçerli token kullanıcı ve oturum id'si ile kimlik doğrulamalı")
  void validToken_setsUserSessionAuthenticationWithUserAndSessionId() throws Exception {
    Users user = new Users();
    user.setId(7L);
    user.setEmail("u@test.com");
    UserSession session = new UserSession();
    session.setId(42L);
    session.setUser(user);
    when(sessionService.authenticate("good")).thenReturn(Optional.of(session));

    MockHttpServletRequest request = request("/report/summary");
    request.setCookies(new Cookie("mm_session", "good"));
    MockFilterChain chain = new MockFilterChain();
    UserSessionAuthentication[] seen = new UserSessionAuthentication[1];
    MockFilterChain capturing = new MockFilterChain() {
      @Override
      public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
        seen[0] = (UserSessionAuthentication) SecurityContextHolder.getContext().getAuthentication();
      }
    };

    filter.doFilter(request, new MockHttpServletResponse(), capturing);

    assertNotNull(seen[0]);
    assertEquals(7L, seen[0].getUserId());
    assertEquals(42L, seen[0].getSessionId());
    assertEquals("u@test.com", seen[0].getPrincipal());
    assertNull(chain.getRequest());
  }

  @Test
  @DisplayName("Login yolu filtrelenmemeli")
  void loginPath_isNotFiltered() throws Exception {
    MockFilterChain chain = new MockFilterChain();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request("/auth/login"), response, chain);

    assertNotNull(chain.getRequest());
    assertEquals(200, response.getStatus());
    verifyNoInteractions(sessionService);
  }

  private MockHttpServletRequest request(String path) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
    request.setServletPath(path);
    return request;
  }
}
