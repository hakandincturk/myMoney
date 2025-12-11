package com.hakandincturk.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hakandincturk.security.services.JwtService;
import com.hakandincturk.core.handler.UnauthorizedResponseWriter;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {

        boolean shouldNotCheckPath = shouldNotFilter(request);
        if(shouldNotCheckPath){
          filterChain.doFilter(request, response);
        }
        
        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")){
          UnauthorizedResponseWriter.write(response, "Yetkisiz giriş. Lütfen tekrar giriş yapınız");
          return;
        }

        String token = header.substring(7);
        
        try{
          String email = jwtService.getEmailByToken(token);
          if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if(jwtService.isTokenDateValid(token)){
              // UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
              Long userId = jwtService.exportToken(token, claims -> claims.get("userId", Long.class));
              JwtAuthentication authenticationToken = new JwtAuthentication(email, userDetails, null, userId);

              authenticationToken.setDetails(userDetails);
              SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
          }
        }
        catch(ExpiredJwtException ex){
          // Token expired: write consistent ApiResponse JSON and return
          UnauthorizedResponseWriter.write(response, "Oturumunuzun süresi dolmuştur. Lütfen tekrar giriş yapınız");
          return;
        }
        catch(Exception ex){
          throw new ServletException("JWT processing failed", ex);
        }

        filterChain.doFilter(request, response);
    
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getServletPath();
    List<String> notPermittedPaths = new ArrayList<>(List.of(
      // Auth
      "/auth/login",
      "/auth/register",
      // Swagger & OpenAPI
      "/swagger-ui/**",
      "/swagger-ui.html",
      "/swagger-ui/index.html",
      "/v3/api-docs",
      "/v3/api-docs/**",
      "/v3/api-docs.yaml",
      "/webjars/**",
      "/swagger-resources/**",
      "/configuration/**"
    ));
    
    AntPathMatcher pathMatcher = new AntPathMatcher();
    return notPermittedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
  }
  
}
