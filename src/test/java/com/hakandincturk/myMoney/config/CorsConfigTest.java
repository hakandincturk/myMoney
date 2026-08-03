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
  @DisplayName("Environment değişkeni verilmediğinde tüm origin'lere izin verilmeli")
  void resolveAllowedOrigins_shouldDefaultToWildcard() {
    assertEquals(List.of("*"), registeredPatterns("*"));
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
  @DisplayName("Wildcard ile birlikte allowCredentials açık kalmalı")
  void corsConfiguration_shouldKeepCredentialsEnabledWithWildcard() {
    CorsConfig config = new CorsConfig();
    ReflectionTestUtils.setField(config, "allowedOrigins", "*");

    UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
    CorsConfiguration configuration = source.getCorsConfigurations().get("/**");

    // allowedOrigins("*") + allowCredentials would blow up at runtime; patterns must be used instead
    assertNull(configuration.getAllowedOrigins());
    assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
  }

}
