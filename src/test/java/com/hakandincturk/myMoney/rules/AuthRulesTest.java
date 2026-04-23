package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.services.rules.AuthRules;

@ExtendWith(MockitoExtension.class)
class AuthRulesTest {

  @InjectMocks
  private AuthRules authRules;

  @Mock
  private UserRepository userRepository;

  @Test
  @DisplayName("Email kullanılmıyorsa hata fırlatılmamalı")
  void checkUserEmailExist_shouldNotThrow_whenEmailNotExists() {
    String email = "test@test.com";

    when(userRepository.findByEmailAndIsRemovedFalse(email))
        .thenReturn(Optional.empty());

    assertDoesNotThrow(() -> authRules.checkUserEmailExist(email));
    verify(userRepository).findByEmailAndIsRemovedFalse(email);
  }

  @Test
  @DisplayName("Email zaten kullanılıyorsa ConflictException fırlatılmalı")
  void checkUserEmailExist_shouldThrowConflictException_whenEmailExists() {
    String email = "test@test.com";
    Users existingUser = new Users();
    existingUser.setEmail(email);

    when(userRepository.findByEmailAndIsRemovedFalse(email))
        .thenReturn(Optional.of(existingUser));

    ConflictException exception = assertThrows(ConflictException.class,
        () -> authRules.checkUserEmailExist(email));

    assertEquals("Bu email zaten kullanimda", exception.getMessage());
  }
}
