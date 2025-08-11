package com.hakandincturk.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthentication extends UsernamePasswordAuthenticationToken {

  private final Long userId;

  public JwtAuthentication(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, Long userId) {
    super(principal, credentials, authorities);
    this.userId = userId;
  }

  public Long getUserId() {
    return userId;
  }

  
}
