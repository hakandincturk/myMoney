package com.hakandincturk.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/** The authenticated caller: user id plus the id of the server-side session the request came with. */
public class UserSessionAuthentication extends UsernamePasswordAuthenticationToken {

  private final Long userId;
  private final Long sessionId;

  public UserSessionAuthentication(Object principal, Collection<? extends GrantedAuthority> authorities, Long userId, Long sessionId) {
    super(principal, null, authorities);
    this.userId = userId;
    this.sessionId = sessionId;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getSessionId() {
    return sessionId;
  }
}
