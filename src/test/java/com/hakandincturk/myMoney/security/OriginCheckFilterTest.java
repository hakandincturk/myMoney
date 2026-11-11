package com.hakandincturk.myMoney.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hakandincturk.core.config.CorsConfig;
import com.hakandincturk.core.handler.UnauthorizedResponseWriter;
import com.hakandincturk.security.OriginCheckFilter;

class OriginCheckFilterTest {

  private OriginCheckFilter filter;

  @BeforeEach
  void setUp() {
    CorsConfig cors = new CorsConfig();
    ReflectionTestUtils.setField(cors, "allowedOrigins", "https://mymoney.hakandincturk.com");
    filter = new OriginCheckFilter(cors.corsConfigurationSource(),
        new UnauthorizedResponseWriter(new ObjectMapper().findAndRegisterModules()));
  }

  @Test
  @DisplayName("Yabancı origin'den POST 403 dönmeli")
  void post_fromForeignOrigin_returns403() throws Exception {
    MockHttpServletRequest request = request("POST", "/transaction/");
    request.addHeader("Origin", "https://evil.example");

    MockHttpServletResponse response = run(request);

    assertEquals(403, response.getStatus());
    assertTrue(response.getContentAsString().contains("İstek kaynağı doğrulanamadı"));
  }

  @Test
  @DisplayName("İzinli origin'den POST geçmeli")
  void post_fromAllowedOrigin_passes() throws Exception {
    MockHttpServletRequest request = request("DELETE", "/auth/sessions/3");
    request.addHeader("Origin", "https://mymoney.hakandincturk.com");

    assertPassed(request);
  }

  @Test
  @DisplayName("Origin yoksa izinli Referer ile POST geçmeli")
  void post_withRefererOnly_allowed_passes() throws Exception {
    MockHttpServletRequest request = request("PUT", "/account/my/1");
    request.addHeader("Referer", "https://mymoney.hakandincturk.com/accounts?page=2");

    assertPassed(request);
  }

  @Test
  @DisplayName("Origin ve Referer yoksa POST 403 dönmeli")
  void post_withoutOriginOrReferer_returns403() throws Exception {
    assertEquals(403, run(request("POST", "/transaction/")).getStatus());
  }

  @Test
  @DisplayName("GET isteği yabancı origin'den de geçmeli")
  void get_fromForeignOrigin_passes() throws Exception {
    MockHttpServletRequest request = request("GET", "/report/summary");
    request.addHeader("Origin", "https://evil.example");

    assertPassed(request);
  }

  @Test
  @DisplayName("Login POST origin olmadan geçmeli")
  void loginPost_withoutOrigin_passes() throws Exception {
    assertPassed(request("POST", "/auth/login"));
  }

  private void assertPassed(MockHttpServletRequest request) throws Exception {
    MockFilterChain chain = new MockFilterChain();
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, chain);
    assertNotNull(chain.getRequest(), "chain should continue");
    assertEquals(200, response.getStatus());
  }

  private MockHttpServletResponse run(MockHttpServletRequest request) throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, new MockFilterChain());
    return response;
  }

  private MockHttpServletRequest request(String method, String path) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    request.setServletPath(path);
    return request;
  }
}
