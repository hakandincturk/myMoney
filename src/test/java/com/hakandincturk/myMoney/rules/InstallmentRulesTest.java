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

import com.hakandincturk.core.enums.InstallmentStatuses;
import com.hakandincturk.core.exception.BusinessException;
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

  @Test
  @DisplayName("Tek taksit mevcut olduğunda döndürülmeli")
  void checkUserSingleInstallmentExistAndGet_shouldReturnInstallment_whenExists() {
    Long userId = 1L;
    Long installmentId = 10L;
    Installment installment = new Installment();
    installment.setId(installmentId);

    when(installmentRepository.findByIdAndTransactionUserIdAndIsRemovedFalse(installmentId, userId))
        .thenReturn(Optional.of(installment));

    Installment result = installmentRules.checkUserSingleInstallmentExistAndGet(userId, installmentId);

    assertNotNull(result);
    assertEquals(installmentId, result.getId());
  }

  @Test
  @DisplayName("Tek taksit bulunamadığında NotFoundException fırlatılmalı")
  void checkUserSingleInstallmentExistAndGet_shouldThrowNotFoundException_whenNotFound() {
    Long userId = 1L;
    Long installmentId = 10L;

    when(installmentRepository.findByIdAndTransactionUserIdAndIsRemovedFalse(installmentId, userId))
        .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> installmentRules.checkUserSingleInstallmentExistAndGet(userId, installmentId));

    assertEquals("Taksit bulunamadı", exception.getMessage());
  }

  @Test
  @DisplayName("Ödenmiş taksit SKIPPED yapılmak istendiğinde BusinessException fırlatılmalı")
  void checkInstallmentCanBeUpdated_shouldThrow_whenPaidAndSkipped() {
    Installment installment = new Installment();
    installment.setPaid(true);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> installmentRules.checkInstallmentCanBeUpdated(installment, InstallmentStatuses.SKIPPED));

    assertEquals("Ödenmiş bir taksit ödenmeyecek olarak işaretlenemez", exception.getMessage());
  }

  @Test
  @DisplayName("SKIPPED durumdaki taksidin tutarı değiştirilmek istendiğinde BusinessException fırlatılmalı")
  void checkInstallmentCanBeUpdated_shouldThrow_whenSkippedAndAmountUpdate() {
    Installment installment = new Installment();
    installment.setPaid(false);
    installment.setStatus(InstallmentStatuses.SKIPPED);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> installmentRules.checkInstallmentCanBeUpdated(installment, null));

    assertEquals("Ödenmeyecek durumdaki bir taksidin tutarı değiştirilemez, önce aktif yapınız", exception.getMessage());
  }

  @Test
  @DisplayName("Aktif ve ödenmemiş taksit güncellenebilmeli - hata fırlatmamalı")
  void checkInstallmentCanBeUpdated_shouldNotThrow_whenActiveAndUnpaid() {
    Installment installment = new Installment();
    installment.setPaid(false);
    installment.setStatus(InstallmentStatuses.ACTIVE);

    assertDoesNotThrow(() -> installmentRules.checkInstallmentCanBeUpdated(installment, InstallmentStatuses.SKIPPED));
  }

  @Test
  @DisplayName("SKIPPED taksit ACTIVE yapılabilmeli - hata fırlatmamalı")
  void checkInstallmentCanBeUpdated_shouldNotThrow_whenSkippedToActive() {
    Installment installment = new Installment();
    installment.setPaid(false);
    installment.setStatus(InstallmentStatuses.SKIPPED);

    assertDoesNotThrow(() -> installmentRules.checkInstallmentCanBeUpdated(installment, InstallmentStatuses.ACTIVE));
  }
}
