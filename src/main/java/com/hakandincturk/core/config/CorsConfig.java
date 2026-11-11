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
	 * Defaults to the local dev origins when CORS_ALLOWED_ORIGINS is not supplied; production sets it
	 * in /opt/mymoney/config/.env.
	 */
	@Value("${cors.allowed-origins:" + DEFAULT_ALLOWED_ORIGINS + "}")
	private String allowedOrigins;

	/**
	 * Local Vite dev/preview origins. No wildcard: session cookies are sent with credentials and
	 * the origin check (OriginCheckFilter) uses this list, so an unset value must fail closed.
	 */
	public static final String DEFAULT_ALLOWED_ORIGINS = "http://localhost:5173,http://localhost:5174";

	/**
	 * Splits the configured value and drops blank entries, so a trailing comma or stray
	 * whitespace in the .env file cannot produce an empty origin that matches nothing.
	 */
	private List<String> resolveAllowedOrigins() {
		List<String> origins = new ArrayList<>();
		for (String origin : allowedOrigins.split(",")) {
			String trimmed = origin.trim();
			if (trimmed.contains("*")) {
				// A wildcard would make OriginCheckFilter accept every origin and silently switch off the
				// CSRF guard for cookie sessions (SameSite=Strict still sends the cookie from sibling
				// *.hakandincturk.com sites). Refuse to start instead.
				throw new IllegalStateException(
					"cors.allowed-origins must list exact origins; wildcards are not allowed: " + trimmed);
			}
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
					// allowedOriginPatterns with exact origins only (wildcards are rejected above).
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
