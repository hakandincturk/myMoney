package com.hakandincturk.services.abstracts;

import java.util.List;
import java.util.Optional;

import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;
import com.hakandincturk.security.session.ClientInfo;

public interface SessionService {

  /** The raw token is returned once, for the cookie; only its hash is stored. */
  record CreatedSession(String rawToken, UserSession session) {
  }

  CreatedSession create(Users user, ClientInfo client);

  /** The live session for this raw token, or empty when unknown, revoked, idle, expired or the user is removed. */
  Optional<UserSession> authenticate(String rawToken);

  List<UserSession> listActive(Long userId);

  /** Revokes one of the user's live sessions; throws NotFoundException when it is not theirs or not live. */
  void revoke(Long userId, Long sessionId);

  /** Revokes the caller's own session; a no-op when it is already revoked. */
  void revokeCurrent(Long sessionId);

  /** Revokes all of the user's sessions except {@code currentSessionId} (null = all). Returns the count. */
  int revokeAllExcept(Long userId, Long currentSessionId);
}
