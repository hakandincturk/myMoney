package com.hakandincturk.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.UserSession;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

  Optional<UserSession> findByTokenHashAndRevokedAtIsNull(String tokenHash);

  List<UserSession> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterAndLastSeenAtAfterOrderByLastSeenAtDesc(
      Long userId, Instant now, Instant idleCutoff);

  Optional<UserSession> findByIdAndUserIdAndRevokedAtIsNull(Long id, Long userId);

  /** Revokes every live session of the user except {@code exceptId}; a null {@code exceptId} revokes all. */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update UserSession s set s.revokedAt = :now "
      + "where s.user.id = :userId and s.revokedAt is null "
      + "and (:exceptId is null or s.id <> :exceptId)")
  int revokeAllForUserExcept(@Param("userId") Long userId, @Param("exceptId") Long exceptId, @Param("now") Instant now);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update UserSession s set s.lastSeenAt = :now where s.id = :id")
  int touch(@Param("id") Long id, @Param("now") Instant now);
}
