package com.hakandincturk.security.session;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/** Session lifetimes and cookie settings ({@code app.session.*}). */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.session")
public class SessionProperties {

  /** A session unused for longer than this is no longer valid. */
  private Duration idleTimeout = Duration.ofDays(7);

  /** A session is never valid longer than this after login, however active. */
  private Duration absoluteTimeout = Duration.ofDays(30);

  /** last_seen_at is written at most this often, so requests do not each cause a write. */
  private Duration touchInterval = Duration.ofMinutes(5);

  /** False only for local development over plain http. */
  private boolean cookieSecure = true;

  private String cookieName = "mm_session";
}
