package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.services.rules.UserRules;

@ExtendWith(MockitoExtension.class)
class UserRulesTest {

  @InjectMocks
  private UserRules userRules;

  @Mock
  private UserRepository userRepository;

  @Test
  @DisplayName("Kullanıcı mevcut olduğunda başarıyla döndürülmeli")
  void checkUserExistAndGet_shouldReturnUser_whenExists() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    when(userRepository.findByIdAndIsRemovedFalse(userId))
        .thenReturn(Optional.of(user));

    Users result = userRules.checkUserExistAndGet(userId);

    assertNotNull(result);
    assertEquals(userId, result.getId());
  }

  @Test
  @DisplayName("Kullanıcı bulunamadığında NotFoundException fırlatılmalı")
  void checkUserExistAndGet_shouldThrowNotFoundException_whenNotExists() {
    Long userId = 1L;

    when(userRepository.findByIdAndIsRemovedFalse(userId))
        .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> userRules.checkUserExistAndGet(userId));

    assertEquals("Kullanıcı bilgileri bulunamadı", exception.getMessage());
  }

  @Test
  @DisplayName("Tüm kullanıcılar mevcut olduğunda liste döndürülmeli")
  void checkAllUsersExistAndGet_shouldReturnUsers_whenAllExist() {
    List<Long> userIds = List.of(1L, 2L, 3L);
    Users user1 = new Users();
    user1.setId(1L);
    Users user2 = new Users();
    user2.setId(2L);
    Users user3 = new Users();
    user3.setId(3L);

    when(userRepository.findByIdInAndIsRemovedFalse(userIds))
        .thenReturn(List.of(user1, user2, user3));

    List<Users> result = userRules.checkAllUsersExistAndGet(userIds);

    assertEquals(3, result.size());
  }

  @Test
  @DisplayName("Bazı kullanıcılar bulunamadığında NotFoundException fırlatılmalı")
  void checkAllUsersExistAndGet_shouldThrowNotFoundException_whenSomeMissing() {
    List<Long> userIds = List.of(1L, 2L, 3L);
    Users user1 = new Users();
    user1.setId(1L);

    when(userRepository.findByIdInAndIsRemovedFalse(userIds))
        .thenReturn(List.of(user1));

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> userRules.checkAllUsersExistAndGet(userIds));

    assertEquals("Kullanıcı bilgileri bulunamadı", exception.getMessage());
  }
}
