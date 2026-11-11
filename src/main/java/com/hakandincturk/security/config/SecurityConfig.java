package com.hakandincturk.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.hakandincturk.core.handler.AuthEntryPoint;
import com.hakandincturk.security.OriginCheckFilter;
import com.hakandincturk.security.SessionAuthenticationFilter;

import com.hakandincturk.security.session.SessionProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SessionProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsConfigurationSource corsConfigurationSource;

  private final String LOGIN_PATH = "/auth/login";
  private final String REGISTER_PATH = "/auth/register";
  
  public static final String[] SWAGGER_PATHS = {
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/swagger-ui/index.html",
    "/v3/api-docs",
    "/v3/api-docs/**",
    "/v3/api-docs.yaml",
    "/webjars/**",
    "/swagger-resources/**",
    "/configuration/**",
  };

  private final AuthenticationProvider authenticationProvider;
  private final OriginCheckFilter originCheckFilter;
  private final SessionAuthenticationFilter sessionAuthenticationFilter;
  private final AuthEntryPoint authEntryPoint;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
    http
      // Spring's CSRF token is not used: SameSite=Strict cookie + OriginCheckFilter cover it.
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(corsConfigurationSource))
      .authorizeHttpRequests(
        request ->
          request
          .requestMatchers(LOGIN_PATH, REGISTER_PATH).permitAll()
          .requestMatchers(SWAGGER_PATHS).permitAll()
          // .anyRequest().permitAll()
          .anyRequest().authenticated()
      )
      .exceptionHandling(exceptionHandler -> exceptionHandler.authenticationEntryPoint(authEntryPoint))
      .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authenticationProvider(authenticationProvider)
      // Origin check first (CSRF guard for cookie auth), then resolve the session cookie.
      .addFilterBefore(originCheckFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterAfter(sessionAuthenticationFilter, OriginCheckFilter.class);

      return http.build();
  }

  // Both filters are @Component beans, so Spring Boot would also register them as plain servlet
  // filters. They must run only inside the security chain (fixed order, after CORS).
  @Bean
  public FilterRegistrationBean<SessionAuthenticationFilter> sessionAuthenticationFilterRegistration(SessionAuthenticationFilter filter) {
    FilterRegistrationBean<SessionAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<OriginCheckFilter> originCheckFilterRegistration(OriginCheckFilter filter) {
    FilterRegistrationBean<OriginCheckFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
