package com.hakandincturk.myMoney.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hakandincturk.security.OriginCheckFilter;
import com.hakandincturk.security.SessionAuthenticationFilter;

import jakarta.servlet.http.Cookie;

/** End-to-end: login, use the cookie, revoke from another session, logout. Real filters and DB. */
@AutoConfigureMockMvc
class SessionFlowIntegrationTest extends BaseIntegrationTest {

  private static final String ORIGIN = "http://localhost:5174";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ApplicationContext context;

  @Test
  @DisplayName("Oturum ve origin filtreleri yalnız güvenlik zincirinde çalışmalı, servlet filtresi olarak kayıtlı olmamalı")
  void authFilters_areNotRegisteredAsServletFilters() {
    int disabled = 0;
    for (FilterRegistrationBean<?> registration : context.getBeansOfType(FilterRegistrationBean.class).values()) {
      Object filter = registration.getFilter();
      if (filter instanceof SessionAuthenticationFilter || filter instanceof OriginCheckFilter) {
        assertFalse(registration.isEnabled(), filter.getClass().getSimpleName());
        disabled++;
      }
    }
    assertEquals(2, disabled, "both filters need a disabled FilterRegistrationBean");
  }

  @Test
  @DisplayName("Başka oturumdan kapatılan oturumun sonraki isteği 401 almalı, diğeri çalışmaya devam etmeli")
  void revokedFromSecondSession_firstSessionGets401() throws Exception {
    String email = register("flow-revoke");
    Cookie a = login(email);
    Cookie b = login(email);

    JsonNode sessions = json(mockMvc.perform(withPath(get("/auth/sessions"), "/auth/sessions").cookie(a)).andReturn().getResponse());
    assertEquals(2, sessions.get("data").size());
    long aId = -1;
    for (JsonNode s : sessions.get("data")) {
      if (s.get("current").asBoolean()) aId = s.get("id").asLong();
    }
    assertTrue(aId > 0, "session A must be marked current");

    MockHttpServletResponse revoke = mockMvc.perform(withPath(delete("/auth/sessions/" + aId), "/auth/sessions/" + aId)
        .cookie(b).header("Origin", ORIGIN)).andReturn().getResponse();
    assertEquals(200, revoke.getStatus(), revoke.getContentAsString());

    MockHttpServletResponse meA = mockMvc.perform(withPath(get("/auth/me"), "/auth/me").cookie(a)).andReturn().getResponse();
    assertEquals(401, meA.getStatus());
    assertTrue(meA.getContentAsString().contains("Oturumunuzun süresi dolmuştur"));

    MockHttpServletResponse meB = mockMvc.perform(withPath(get("/auth/me"), "/auth/me").cookie(b)).andReturn().getResponse();
    assertEquals(200, meB.getStatus());
    assertEquals(email, json(meB).get("data").get("email").asText());
  }

  @Test
  @DisplayName("Çıkıştan sonra aynı cookie 401 almalı")
  void logout_thenSameCookie_gets401() throws Exception {
    Cookie a = login(register("flow-logout"));

    MockHttpServletResponse logout = mockMvc.perform(withPath(post("/auth/logout"), "/auth/logout")
        .cookie(a).header("Origin", ORIGIN)).andReturn().getResponse();
    assertEquals(200, logout.getStatus(), logout.getContentAsString());
    assertTrue(logout.getHeader("Set-Cookie").contains("Max-Age=0"));

    assertEquals(401, mockMvc.perform(withPath(get("/auth/me"), "/auth/me").cookie(a)).andReturn().getResponse().getStatus());
  }

  @Test
  @DisplayName("Yabancı origin'den gelen yazma isteği geçerli cookie ile bile 403 almalı")
  void foreignOriginWrite_withValidCookie_gets403() throws Exception {
    Cookie a = login(register("flow-origin"));

    MockHttpServletResponse res = mockMvc.perform(withPath(post("/auth/logout"), "/auth/logout")
        .cookie(a).header("Origin", "https://evil.example")).andReturn().getResponse();

    assertEquals(403, res.getStatus());
    assertEquals(200, mockMvc.perform(withPath(get("/auth/me"), "/auth/me").cookie(a)).andReturn().getResponse().getStatus());
  }

  private String register(String prefix) throws Exception {
    String email = prefix + "-" + System.nanoTime() + "@test.com";
    String body = objectMapper.writeValueAsString(Map.of(
        "fullName", "Flow User", "email", email, "password", "Secret-123", "phone", "5550000000"));
    MockHttpServletResponse res = mockMvc.perform(withPath(post("/auth/register"), "/auth/register")
        .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
    assertEquals(200, res.getStatus(), res.getContentAsString());
    return email;
  }

  private Cookie login(String email) throws Exception {
    String body = objectMapper.writeValueAsString(Map.of("email", email, "password", "Secret-123"));
    MockHttpServletResponse res = mockMvc.perform(withPath(post("/auth/login"), "/auth/login")
        .contentType(MediaType.APPLICATION_JSON).content(body)
        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"))
        .andReturn().getResponse();
    assertEquals(200, res.getStatus(), res.getContentAsString());
    assertFalse(res.getContentAsString().contains("token"), "login body must not carry a token");
    Cookie cookie = res.getCookie("mm_session");
    assertNotNull(cookie, "login must set mm_session");
    assertTrue(cookie.isHttpOnly());
    return new Cookie("mm_session", cookie.getValue());
  }

  private MockHttpServletRequestBuilder withPath(MockHttpServletRequestBuilder builder, String servletPath) {
    return builder.servletPath(servletPath);
  }

  private JsonNode json(MockHttpServletResponse res) throws Exception {
    return objectMapper.readTree(res.getContentAsString());
  }
}
