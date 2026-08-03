package com.hakandincturk.core.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

	/**
	 * Comma-separated list of allowed origins, e.g. "https://domain1.com,https://domain2.com".
	 * Defaults to "*" (every origin) when CORS_ALLOWED_ORIGINS is not supplied, so the same jar
	 * runs unchanged on localhost, on a bare IP, or behind a domain without a rebuild.
	 */
	@Value("${cors.allowed-origins:*}")
	private String allowedOrigins;

	/**
	 * Splits the configured value and drops blank entries, so a trailing comma or stray
	 * whitespace in the .env file cannot produce an empty origin that matches nothing.
	 */
	private List<String> resolveAllowedOrigins() {
		List<String> origins = new ArrayList<>();
		for (String origin : allowedOrigins.split(",")) {
			String trimmed = origin.trim();
			if (!trimmed.isEmpty()) {
				origins.add(trimmed);
			}
		}
		return origins;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		final String[] originPatterns = resolveAllowedOrigins().toArray(new String[0]);

		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(@NonNull CorsRegistry registry) {
				registry.addMapping("/**")
					// allowedOriginPatterns instead of allowedOrigins: Spring rejects the literal "*"
					// in allowedOrigins while allowCredentials is true. Patterns accept both "*" and
					// exact origins, so a single code path covers every configuration.
					.allowedOriginPatterns(originPatterns)
					.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
					.allowedHeaders("*")
					.allowCredentials(true);
			}
		};
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(resolveAllowedOrigins());
		configuration.setAllowedMethods(List.of("GET","POST","PUT","PATCH", "DELETE","OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}


}
