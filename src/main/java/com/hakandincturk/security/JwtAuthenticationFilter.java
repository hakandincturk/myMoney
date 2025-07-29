package com.hakandincturk.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hakandincturk.security.services.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")){
          filterChain.doFilter(request, response);
          return;
        }

        String token = header.substring(7);
        
        try{
          String email = jwtService.getEmailByToken(token);
          if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if(jwtService.isTokenDateValid(token)){
              UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

              authenticationToken.setDetails(userDetails);
              SecurityContextHolder.getContext().setAuthentication(authenticationToken);
              
            }

          }

        }
        catch(ExpiredJwtException ex){
          throw new ExpiredJwtException(ex.getHeader(), ex.getClaims(), "JWT token has expired", ex);
        }
        catch(Exception ex){
          throw new ServletException("JWT processing failed", ex);
        }

        filterChain.doFilter(request, response);
    
  }
  
}
