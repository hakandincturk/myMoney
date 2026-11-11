package com.hakandincturk.dtos.auth.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One row of the "active sessions" list. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponseDto {
  private Long id;
  private String deviceLabel;
  private String ipAddress;
  private String country;
  private Instant createdAt;
  private Instant lastSeenAt;
  /** True for the session this request came with ("Bu cihaz"). */
  private boolean current;
}
