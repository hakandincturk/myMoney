package com.hakandincturk.myMoney.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.repositories.UserSessionRepository;

@Transactional
class UserSessionRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserSessionRepository sessionRepository;

  @Autowired
  private UserRepository userRepository;

  // Postgres timestamptz keeps microseconds; truncate so equality checks are stable.
  private final Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
  private final Instant idleCutoff = now.minus(Duration.ofDays(7));

  private Users user;
  private Users otherUser;

  @BeforeEach
  void setUp() {
    user = userRepository.save(newUser("session-user@test.com"));
    otherUser = userRepository.save(newUser("session-other@test.com"));
  }

  @Test
  @DisplayName("Token hash ile arama iptal edilmiş oturumu döndürmemeli")
  void findByTokenHash_ignoresRevoked() {
    UserSession active = sessionRepository.save(newSession(user, "a", now, now.plus(Duration.ofDays(30))));
    UserSession revoked = newSession(user, "b", now, now.plus(Duration.ofDays(30)));
    revoked.setRevokedAt(now);
    sessionRepository.save(revoked);

    assertEquals(active.getId(), sessionRepository.findByTokenHashAndRevokedAtIsNull(hash("a")).orElseThrow().getId());
    assertTrue(sessionRepository.findByTokenHashAndRevokedAtIsNull(hash("b")).isEmpty());
  }

  @Test
  @DisplayName("Aktif oturum listesi süresi dolmuş, boşta kalmış ve iptal edilmiş oturumları dışlamalı, en yeni önce gelmeli")
  void listActive_excludesExpiredIdleAndRevoked_newestFirst() {
    UserSession older = sessionRepository.save(newSession(user, "older", now.minus(Duration.ofHours(2)), now.plus(Duration.ofDays(30))));
    UserSession newer = sessionRepository.save(newSession(user, "newer", now.minus(Duration.ofMinutes(1)), now.plus(Duration.ofDays(30))));
    sessionRepository.save(newSession(user, "expired", now, now.minusSeconds(1)));
    sessionRepository.save(newSession(user, "idle", now.minus(Duration.ofDays(8)), now.plus(Duration.ofDays(30))));
    UserSession revoked = newSession(user, "revoked", now, now.plus(Duration.ofDays(30)));
    revoked.setRevokedAt(now);
    sessionRepository.save(revoked);
    sessionRepository.save(newSession(otherUser, "other", now, now.plus(Duration.ofDays(30))));

    List<UserSession> result = sessionRepository
        .findByUserIdAndRevokedAtIsNullAndExpiresAtAfterAndLastSeenAtAfterOrderByLastSeenAtDesc(user.getId(), now, idleCutoff);

    assertEquals(List.of(newer.getId(), older.getId()), result.stream().map(UserSession::getId).toList());
  }

  @Test
  @DisplayName("Id ve kullanıcı ile arama başka kullanıcının oturumunu döndürmemeli")
  void findByIdAndUserId_doesNotReturnOtherUsersRow() {
    UserSession others = sessionRepository.save(newSession(otherUser, "x", now, now.plus(Duration.ofDays(30))));

    assertTrue(sessionRepository.findByIdAndUserIdAndRevokedAtIsNull(others.getId(), user.getId()).isEmpty());
    assertTrue(sessionRepository.findByIdAndUserIdAndRevokedAtIsNull(others.getId(), otherUser.getId()).isPresent());
  }

  @Test
  @DisplayName("Diğer oturumları kapatma mevcut oturumu ve başka kullanıcıları korumalı")
  void revokeAllForUserExcept_keepsCurrentAndOtherUsers() {
    UserSession current = sessionRepository.save(newSession(user, "c", now, now.plus(Duration.ofDays(30))));
    UserSession second = sessionRepository.save(newSession(user, "d", now, now.plus(Duration.ofDays(30))));
    UserSession third = sessionRepository.save(newSession(user, "e", now, now.plus(Duration.ofDays(30))));
    UserSession others = sessionRepository.save(newSession(otherUser, "f", now, now.plus(Duration.ofDays(30))));

    int count = sessionRepository.revokeAllForUserExcept(user.getId(), current.getId(), now);

    assertEquals(2, count);
    assertNull(sessionRepository.findById(current.getId()).orElseThrow().getRevokedAt());
    assertNotNull(sessionRepository.findById(second.getId()).orElseThrow().getRevokedAt());
    assertNotNull(sessionRepository.findById(third.getId()).orElseThrow().getRevokedAt());
    assertNull(sessionRepository.findById(others.getId()).orElseThrow().getRevokedAt());
  }

  @Test
  @DisplayName("Mevcut oturum verilmezse kullanıcının tüm oturumları kapanmalı")
  void revokeAllForUserExcept_nullExcept_revokesAll() {
    sessionRepository.save(newSession(user, "g", now, now.plus(Duration.ofDays(30))));
    sessionRepository.save(newSession(user, "h", now, now.plus(Duration.ofDays(30))));

    assertEquals(2, sessionRepository.revokeAllForUserExcept(user.getId(), null, now));
  }

  @Test
  @DisplayName("Touch son görülme zamanını güncellemeli")
  void touch_updatesLastSeen() {
    UserSession s = sessionRepository.save(newSession(user, "t", now.minus(Duration.ofHours(1)), now.plus(Duration.ofDays(30))));

    assertEquals(1, sessionRepository.touch(s.getId(), now));
    assertEquals(now, sessionRepository.findById(s.getId()).orElseThrow().getLastSeenAt());
  }

  private Users newUser(String email) {
    Users u = new Users();
    u.setFullName("Session User");
    u.setEmail(email);
    u.setPassword("encoded");
    u.setPhone("5550000000");
    return u;
  }

  private UserSession newSession(Users owner, String token, Instant lastSeen, Instant expires) {
    UserSession s = new UserSession();
    s.setUser(owner);
    s.setTokenHash(hash(token));
    s.setCreatedAt(lastSeen);
    s.setLastSeenAt(lastSeen);
    s.setExpiresAt(expires);
    s.setUserAgent("JUnit");
    s.setDeviceLabel("Test");
    s.setIpAddress("127.0.0.1");
    return s;
  }

  // Deterministic 64-char stand-in for a SHA-256 hex digest.
  private String hash(String token) {
    return String.format("%-64s", token).replace(' ', '0');
  }
}
