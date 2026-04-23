package com.hakandincturk.myMoney.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.InstallmentRepository;
import com.hakandincturk.services.rules.InstallmentRules;
import com.hakandincturk.services.rules.UserRules;

@ExtendWith(MockitoExtension.class)
class InstallmentRulesTest {

  @InjectMocks
  private InstallmentRules installmentRules;

  @Mock
  private InstallmentRepository installmentRepository;

  @Mock
  private UserRules userRules;

  @Test
  @DisplayName("Tüm taksitler mevcut olduğunda liste döndürülmeli")
  void checkUserInstallmentExistAndGet_shouldReturnInstallments_whenAllExist() {
    Long userId = 1L;
    List<Long> installmentIds = List.of(10L, 20L);
    Installment i1 = new Installment();
    i1.setId(10L);
    Installment i2 = new Installment();
    i2.setId(20L);

    when(installmentRepository.findByIdInAndTransactionUserIdAndIsRemovedFalse(installmentIds, userId))
        .thenReturn(List.of(i1, i2));

    List<Installment> result = installmentRules.checkUserInstallmentExistAndGet(userId, installmentIds);

    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("Bazı taksitler bulunamadığında NotFoundException fırlatılmalı")
  void checkUserInstallmentExistAndGet_shouldThrowNotFoundException_whenSomeMissing() {
    Long userId = 1L;
    List<Long> installmentIds = List.of(10L, 20L, 30L);
    Installment i1 = new Installment();
    i1.setId(10L);

    when(installmentRepository.findByIdInAndTransactionUserIdAndIsRemovedFalse(installmentIds, userId))
        .thenReturn(List.of(i1));

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> installmentRules.checkUserInstallmentExistAndGet(userId, installmentIds));

    assertEquals("Taksitler bulunamadı", exception.getMessage());
  }

  @Test
  @DisplayName("userId null olmadığında kullanıcı doğrulanıp döndürülmeli")
  void getValidatedUser_shouldReturnUser_whenUserIdNotNull() {
    Long userId = 1L;
    Users user = new Users();
    user.setId(userId);

    when(userRules.checkUserExistAndGet(userId)).thenReturn(user);

    Users result = installmentRules.getValidatedUser(userId);

    assertNotNull(result);
    assertEquals(userId, result.getId());
  }

  @Test
  @DisplayName("userId null olduğunda null döndürülmeli")
  void getValidatedUser_shouldReturnNull_whenUserIdNull() {
    Users result = installmentRules.getValidatedUser(null);

    assertNull(result);
    verifyNoInteractions(userRules);
  }
}
