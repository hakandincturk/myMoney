package com.hakandincturk.security;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
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
        
        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")){
          filterChain.doFilter(request, response);
          // UnauthorizedResponseWriter.write(response, "Oturumunuzun süresi dolmuştur. Lütfen tekrar giriş yapınız");
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
  
}
