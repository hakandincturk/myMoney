package com.hakandincturk.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hakandincturk.core.handler.AuthEntryPoint;
import com.hakandincturk.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  
  private final String LOGIN_PATH = "/api/auth/login";
  private final String REGISTER_PATH = "/api/auth/register";
  
  public static final String[] SWAGGER_PATHS = {
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/swagger-ui/index.html",
    "/v3/api-docs/**",
    "/v3/api-docs.yaml",
    "/webjars/**",
    "/swagger-resources/**",
    "/configuration/**"
  };

  @Autowired
  private AuthenticationProvider authenticationProvider;

  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired
  private AuthEntryPoint authEntryPoint;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
    http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.disable())
      .authorizeHttpRequests(
        request ->
          request
          .requestMatchers(LOGIN_PATH, REGISTER_PATH).permitAll()
          .requestMatchers(SWAGGER_PATHS).permitAll()
          .anyRequest().authenticated()
      )
      .exceptionHandling(exceptionHandler -> exceptionHandler.authenticationEntryPoint(authEntryPoint))
      .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authenticationProvider(authenticationProvider)
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
  }

}
