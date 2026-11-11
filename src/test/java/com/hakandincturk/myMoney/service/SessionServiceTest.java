package com.hakandincturk.myMoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.UserSession;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserSessionRepository;
import com.hakandincturk.security.session.ClientInfo;
import com.hakandincturk.security.session.SessionProperties;
import com.hakandincturk.security.session.SessionTokens;
import com.hakandincturk.services.abstracts.SessionService.CreatedSession;
import com.hakandincturk.services.impl.SessionServiceImpl;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

  private static final Instant NOW = Instant.parse("2026-11-11T12:00:00Z");

  @Mock
  private UserSessionRepository repository;

  private SessionServiceImpl service;
  private Users user;

  @BeforeEach
  void setUp() {
    service = new SessionServiceImpl(repository, new SessionProperties(), Clock.fixed(NOW, ZoneOffset.UTC));
    user = new Users();
    user.setId(7L);
    user.setEmail("u@test.com");
  }

  @Test
  @DisplayName("Oturum oluşturma ham token yerine hash saklamalı ve 30 gün sonra bitmeli")
  void create_storesHashNotRawToken_andSetsExpiry30Days() {
    when(repository.save(any(UserSession.class))).thenAnswer(inv -> inv.getArgument(0));
    String chromeMac = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    CreatedSession created = service.create(user, new ClientInfo(chromeMac, "1.2.3.4", "TR"));

    UserSession saved = created.session();
    assertEquals(SessionTokens.hash(created.rawToken()), saved.getTokenHash());
    assertNotEquals(created.rawToken(), saved.getTokenHash());
    assertEquals(NOW, saved.getCreatedAt());
    assertEquals(NOW, saved.getLastSeenAt());
    assertEquals(NOW.plus(Duration.ofDays(30)), saved.getExpiresAt());
    assertEquals("Chrome · macOS", saved.getDeviceLabel());
    assertEquals("1.2.3.4", saved.getIpAddress());
    assertEquals("TR", saved.getCountry());
    assertSame(user, saved.getUser());
  }

  @Test
  @DisplayName("Bilinmeyen ya da iptal edilmiş token reddedilmeli")
  void authenticate_rejectsRevoked() {
    when(repository.findByTokenHashAndRevokedAtIsNull(SessionTokens.hash("raw"))).thenReturn(Optional.empty());

    assertTrue(service.authenticate("raw").isEmpty());
  }

  @Test
  @DisplayName("7 günden uzun boşta kalan oturum reddedilmeli")
  void authenticate_rejectsIdleOver7Days() {
    stubSession(session(NOW.minus(Duration.ofDays(7)).minusSeconds(1), NOW.plus(Duration.ofDays(10))));

    assertTrue(service.authenticate("raw").isEmpty());
    verify(repository, never()).touch(any(), any());
  }

  @Test
  @DisplayName("Mutlak süre dolan oturum yakın zamanda kullanılmış olsa bile reddedilmeli")
  void authenticate_rejectsAfterAbsoluteExpiryEvenIfRecentlySeen() {
    stubSession(session(NOW.minusSeconds(30), NOW));

    assertTrue(service.authenticate("raw").isEmpty());
  }

  @Test
  @DisplayName("Silinmiş kullanıcının oturumu reddedilmeli ve iptal edilmeli")
  void authenticate_rejectsAndRevokesRemovedUser() {
    user.setRemoved(true);
    UserSession s = session(NOW.minusSeconds(30), NOW.plus(Duration.ofDays(1)));
    stubSession(s);

    assertTrue(service.authenticate("raw").isEmpty());
    assertEquals(NOW, s.getRevokedAt());
    verify(repository).save(s);
  }

  @Test
  @DisplayName("Son görülme yalnızca 5 dakikadan eskiyse güncellenmeli")
  void authenticate_touchesOnlyWhenOlderThan5Minutes() {
    UserSession fresh = session(NOW.minus(Duration.ofMinutes(4)), NOW.plus(Duration.ofDays(1)));
    stubSession(fresh);
    assertTrue(service.authenticate("raw").isPresent());
    verify(repository, never()).touch(any(), any());

    UserSession stale = session(NOW.minus(Duration.ofMinutes(6)), NOW.plus(Duration.ofDays(1)));
    stubSession(stale);
    assertTrue(service.authenticate("raw").isPresent());
    verify(repository).touch(stale.getId(), NOW);
  }

  @Test
  @DisplayName("Aktif oturumlar boşta kalma sınırıyla listelenmeli")
  void listActive_usesNowAndIdleCutoff() {
    List<UserSession> rows = List.of(session(NOW, NOW.plus(Duration.ofDays(1))));
    when(repository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterAndLastSeenAtAfterOrderByLastSeenAtDesc(
        7L, NOW, NOW.minus(Duration.ofDays(7)))).thenReturn(rows);

    assertSame(rows, service.listActive(7L));
  }

  @Test
  @DisplayName("Başka kullanıcının oturumunu kapatmak NotFound fırlatmalı")
  void revoke_otherUsersSession_throwsNotFound() {
    when(repository.findByIdAndUserIdAndRevokedAtIsNull(99L, 7L)).thenReturn(Optional.empty());

    NotFoundException ex = assertThrows(NotFoundException.class, () -> service.revoke(7L, 99L));
    assertEquals("Oturum bulunamadı", ex.getMessage());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Kendi oturumunu kapatmak revokedAt atamalı")
  void revoke_ownSession_setsRevokedAt() {
    UserSession s = session(NOW, NOW.plus(Duration.ofDays(1)));
    when(repository.findByIdAndUserIdAndRevokedAtIsNull(s.getId(), 7L)).thenReturn(Optional.of(s));

    service.revoke(7L, s.getId());

    ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
    verify(repository).save(captor.capture());
    assertEquals(NOW, captor.getValue().getRevokedAt());
  }

  @Test
  @DisplayName("Zaten kapalı mevcut oturumu kapatmak hata vermemeli")
  void revokeCurrent_alreadyRevoked_noException() {
    UserSession s = session(NOW, NOW.plus(Duration.ofDays(1)));
    s.setRevokedAt(NOW.minusSeconds(5));
    when(repository.findById(s.getId())).thenReturn(Optional.of(s));

    assertDoesNotThrow(() -> service.revokeCurrent(s.getId()));
    assertEquals(NOW.minusSeconds(5), s.getRevokedAt());
    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("Diğer oturumları kapatma mevcut oturum id'sini repository'ye iletmeli")
  void revokeAllExcept_passesCurrentId() {
    when(repository.revokeAllForUserExcept(7L, 3L, NOW)).thenReturn(2);

    assertEquals(2, service.revokeAllExcept(7L, 3L));
  }

  private void stubSession(UserSession s) {
    when(repository.findByTokenHashAndRevokedAtIsNull(SessionTokens.hash("raw"))).thenReturn(Optional.of(s));
  }

  private long nextId = 1;

  private UserSession session(Instant lastSeen, Instant expires) {
    UserSession s = new UserSession();
    s.setId(nextId++);
    s.setUser(user);
    s.setLastSeenAt(lastSeen);
    s.setCreatedAt(lastSeen);
    s.setExpiresAt(expires);
    return s;
  }
}
