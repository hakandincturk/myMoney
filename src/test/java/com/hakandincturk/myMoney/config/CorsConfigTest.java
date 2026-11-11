package com.hakandincturk.myMoney.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hakandincturk.core.config.CorsConfig;

class CorsConfigTest {

  /**
   * Reads back what the bean actually registered, rather than the raw property, so the test
   * fails if the parsing result never reaches the Spring CORS configuration.
   */
  private List<String> registeredPatterns(String configuredValue) {
    CorsConfig config = new CorsConfig();
    ReflectionTestUtils.setField(config, "allowedOrigins", configuredValue);

    UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
    CorsConfiguration configuration = source.getCorsConfigurations().get("/**");

    return configuration.getAllowedOriginPatterns();
  }

  @Test
  @DisplayName("Environment değişkeni verilmediğinde yalnız lokal geliştirme origin'leri izinli olmalı (wildcard yok)")
  void resolveAllowedOrigins_shouldDefaultToLocalDevOrigins() {
    assertEquals(
      List.of("http://localhost:5173", "http://localhost:5174"),
      registeredPatterns(CorsConfig.DEFAULT_ALLOWED_ORIGINS));
  }

  @Test
  @DisplayName("Virgülle ayrılmış birden fazla origin ayrıştırılmalı")
  void resolveAllowedOrigins_shouldSplitCommaSeparatedValues() {
    assertEquals(
      List.of("https://domain1.com", "https://domain2.com"),
      registeredPatterns("https://domain1.com,https://domain2.com"));
  }

  @Test
  @DisplayName("Origin'lerdeki boşluklar temizlenmeli ve boş girdiler atılmalı")
  void resolveAllowedOrigins_shouldTrimAndDropBlankEntries() {
    assertEquals(
      List.of("https://domain1.com", "https://domain2.com"),
      registeredPatterns(" https://domain1.com , , https://domain2.com ,"));
  }

  @Test
  @DisplayName("Wildcard origin reddedilmeli: cookie oturumunda CSRF korumasını sessizce kapatır")
  void resolveAllowedOrigins_shouldRejectWildcard() {
    for (String value : new String[] {"*", "https://mymoney.hakandincturk.com,*", "https://*.hakandincturk.com"}) {
      CorsConfig config = new CorsConfig();
      ReflectionTestUtils.setField(config, "allowedOrigins", value);

      IllegalStateException ex = assertThrows(IllegalStateException.class, config::corsConfigurationSource, value);
      assertTrue(ex.getMessage().contains("cors.allowed-origins"), ex.getMessage());
    }
  }

  @Test
  @DisplayName("Açık origin listesiyle allowCredentials açık kalmalı ve allowedOrigins kullanılmamalı")
  void corsConfiguration_shouldKeepCredentialsEnabled() {
    CorsConfig config = new CorsConfig();
    ReflectionTestUtils.setField(config, "allowedOrigins", "https://mymoney.hakandincturk.com");

    UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
    CorsConfiguration configuration = source.getCorsConfigurations().get("/**");

    assertNull(configuration.getAllowedOrigins());
    assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
  }

}
