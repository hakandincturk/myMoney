package com.hakandincturk.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmailAndIsRemovedFalse(String email);
  Optional<User> findByIdAndIsRemovedFalse(Long userId);
}
