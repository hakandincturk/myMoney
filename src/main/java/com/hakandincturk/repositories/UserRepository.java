package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
  Optional<Users> findByEmailAndIsRemovedFalse(String email);
  Optional<Users> findByIdAndIsRemovedFalse(Long userId);
  List<Users> findByIdInAndIsRemovedFalse(List<Long> userIds);
  List<Users> findAllByIsRemovedFalse();
}
