package com.hakandincturk.services.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserSessionRepository;
import com.hakandincturk.security.session.ClientInfo;
import com.hakandincturk.security.session.DeviceLabels;
import com.hakandincturk.security.session.SessionProperties;
import com.hakandincturk.security.session.SessionTokens;
import com.hakandincturk.services.abstracts.SessionService;

@Service
public class SessionServiceImpl implements SessionService {

  private final UserSessionRepository repository;
  private final SessionProperties properties;
  private final Clock clock;

  public SessionServiceImpl(UserSessionRepository repository, SessionProperties properties, Clock clock) {
    this.repository = repository;
    this.properties = properties;
    this.clock = clock;
  }

  @Override
  @Transactional
  public CreatedSession create(Users user, ClientInfo client) {
    Instant now = clock.instant();
    String rawToken = SessionTokens.newRawToken();

    UserSession session = new UserSession();
    session.setUser(user);
    session.setTokenHash(SessionTokens.hash(rawToken));
    session.setCreatedAt(now);
    session.setLastSeenAt(now);
    session.setExpiresAt(now.plus(properties.getAbsoluteTimeout()));
    session.setUserAgent(client.userAgent());
    session.setDeviceLabel(DeviceLabels.fromUserAgent(client.userAgent()));
    session.setIpAddress(client.ipAddress());
    session.setCountry(client.country());

    return new CreatedSession(rawToken, repository.save(session));
  }

  @Override
  @Transactional
  public Optional<UserSession> authenticate(String rawToken) {
    Instant now = clock.instant();
    Optional<UserSession> found = repository.findByTokenHashAndRevokedAtIsNull(SessionTokens.hash(rawToken));
    if (found.isEmpty()) {
      return Optional.empty();
    }

    UserSession session = found.get();
    boolean expired = !session.getExpiresAt().isAfter(now);
    boolean idle = !session.getLastSeenAt().isAfter(now.minus(properties.getIdleTimeout()));
    if (expired || idle) {
      return Optional.empty();
    }

    if (session.getUser().isRemoved()) {
      session.setRevokedAt(now);
      repository.save(session);
      return Optional.empty();
    }

    if (session.getLastSeenAt().isBefore(now.minus(properties.getTouchInterval()))) {
      repository.touch(session.getId(), now);
      session.setLastSeenAt(now);
    }
    return Optional.of(session);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserSession> listActive(Long userId) {
    Instant now = clock.instant();
    return repository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterAndLastSeenAtAfterOrderByLastSeenAtDesc(
        userId, now, now.minus(properties.getIdleTimeout()));
  }

  @Override
  @Transactional
  public void revoke(Long userId, Long sessionId) {
    UserSession session = repository.findByIdAndUserIdAndRevokedAtIsNull(sessionId, userId)
        .orElseThrow(() -> new NotFoundException("Oturum bulunamadı"));
    session.setRevokedAt(clock.instant());
    repository.save(session);
  }

  @Override
  @Transactional
  public void revokeCurrent(Long sessionId) {
    repository.findById(sessionId)
        .filter(session -> session.getRevokedAt() == null)
        .ifPresent(session -> {
          session.setRevokedAt(clock.instant());
          repository.save(session);
        });
  }

  @Override
  @Transactional
  public int revokeAllExcept(Long userId, Long currentSessionId) {
    return repository.revokeAllForUserExcept(userId, currentSessionId, clock.instant());
  }
}
