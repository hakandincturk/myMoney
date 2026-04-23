package com.hakandincturk.myMoney.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.hakandincturk.core.enums.sort.AccountSortColumn;
import com.hakandincturk.core.enums.sort.BaseSortColumn;
import com.hakandincturk.core.enums.sort.ContactSortColumn;
import com.hakandincturk.core.enums.sort.InstallmentSortColumn;
import com.hakandincturk.core.enums.sort.TagTransactionColumn;
import com.hakandincturk.core.enums.sort.TransactionSortColumn;

class SortColumnTest {

  @Nested
  @DisplayName("AccountSortColumn")
  class AccountSortColumnTests {

    @Test
    @DisplayName("Enum değerleri doğru entityProperty döndürmeli")
    void shouldReturnCorrectEntityProperty() {
      assertEquals("name", AccountSortColumn.ACCOUNT_NAME.getEntityProperty());
      assertEquals("balance", AccountSortColumn.ACCOUNT_BALANCE.getEntityProperty());
      assertEquals("type", AccountSortColumn.ACCOUNT_TYPE.getEntityProperty());
    }

    @Test
    @DisplayName("Enum değerleri doğru displayName döndürmeli")
    void shouldReturnCorrectDisplayName() {
      assertEquals("accountName", AccountSortColumn.ACCOUNT_NAME.getDisplayName());
      assertEquals("accountBalance", AccountSortColumn.ACCOUNT_BALANCE.getDisplayName());
    }

    @Test
    @DisplayName("fromString ile enum değeri bulunmalı")
    void fromString_shouldReturnCorrectEnum() {
      assertEquals(AccountSortColumn.ACCOUNT_NAME, AccountSortColumn.fromString("accountName"));
      assertEquals(AccountSortColumn.ACCOUNT_BALANCE, AccountSortColumn.fromString("ACCOUNT_BALANCE"));
    }

    @Test
    @DisplayName("fromString geçersiz değer ile hata fırlatmalı")
    void fromString_shouldThrowForInvalidValue() {
      assertThrows(IllegalArgumentException.class, () -> AccountSortColumn.fromString("invalidColumn"));
    }

    @Test
    @DisplayName("Tüm enum değerleri mevcut olmalı")
    void shouldHaveAllValues() {
      assertEquals(6, AccountSortColumn.values().length);
    }
  }

  @Nested
  @DisplayName("ContactSortColumn")
  class ContactSortColumnTests {

    @Test
    @DisplayName("Enum değerleri doğru entityProperty döndürmeli")
    void shouldReturnCorrectEntityProperty() {
      assertEquals("fullName", ContactSortColumn.FULL_NAME.getEntityProperty());
      assertEquals("email", ContactSortColumn.EMAIL.getEntityProperty());
      assertEquals("phone", ContactSortColumn.PHONE.getEntityProperty());
    }

    @Test
    @DisplayName("fromString ile enum değeri bulunmalı")
    void fromString_shouldReturnCorrectEnum() {
      assertEquals(ContactSortColumn.FULL_NAME, ContactSortColumn.fromString("fullName"));
      assertEquals(ContactSortColumn.FULL_NAME, ContactSortColumn.fromString("FULL_NAME"));
    }

    @Test
    @DisplayName("fromString geçersiz değer ile hata fırlatmalı")
    void fromString_shouldThrowForInvalidValue() {
      assertThrows(IllegalArgumentException.class, () -> ContactSortColumn.fromString("nonExistent"));
    }

    @Test
    @DisplayName("Tüm enum değerleri mevcut olmalı")
    void shouldHaveAllValues() {
      assertEquals(5, ContactSortColumn.values().length);
    }
  }

  @Nested
  @DisplayName("InstallmentSortColumn")
  class InstallmentSortColumnTests {

    @Test
    @DisplayName("Enum değerleri doğru entityProperty döndürmeli")
    void shouldReturnCorrectEntityProperty() {
      assertEquals("transaction.name", InstallmentSortColumn.CONTACT_NAME.getEntityProperty());
      assertEquals("debtDate", InstallmentSortColumn.DEBT_DATE.getEntityProperty());
      assertEquals("installmentNumber", InstallmentSortColumn.INSTALLMENT_NUMBER.getEntityProperty());
      assertEquals("amount", InstallmentSortColumn.AMOUNT.getEntityProperty());
      assertEquals("isPaid", InstallmentSortColumn.IS_PAID.getEntityProperty());
      assertEquals("paidDate", InstallmentSortColumn.PAID_DATE.getEntityProperty());
    }

    @Test
    @DisplayName("fromString ile enum değeri bulunmalı")
    void fromString_shouldReturnCorrectEnum() {
      assertEquals(InstallmentSortColumn.DEBT_DATE, InstallmentSortColumn.fromString("debtDate"));
      assertEquals(InstallmentSortColumn.AMOUNT, InstallmentSortColumn.fromString("amount"));
      assertEquals(InstallmentSortColumn.IS_PAID, InstallmentSortColumn.fromString("isPaid"));
    }

    @Test
    @DisplayName("fromString geçersiz değer ile hata fırlatmalı")
    void fromString_shouldThrowForInvalidValue() {
      assertThrows(IllegalArgumentException.class, () -> InstallmentSortColumn.fromString("invalid"));
    }

    @Test
    @DisplayName("Tüm enum değerleri mevcut olmalı")
    void shouldHaveAllValues() {
      assertEquals(6, InstallmentSortColumn.values().length);
    }
  }

  @Nested
  @DisplayName("TagTransactionColumn")
  class TagTransactionColumnTests {

    @Test
    @DisplayName("Enum değerleri doğru entityProperty döndürmeli")
    void shouldReturnCorrectEntityProperty() {
      assertEquals("transaction.account.name", TagTransactionColumn.ACCOUNT_NAME.getEntityProperty());
      assertEquals("transaction.type", TagTransactionColumn.TYPE.getEntityProperty());
      assertEquals("transaction.status", TagTransactionColumn.STATUS.getEntityProperty());
      assertEquals("transaction.totalAmount", TagTransactionColumn.TOTAL_AMOUNT.getEntityProperty());
      assertEquals("transaction.paidAmount", TagTransactionColumn.PAID_AMOUNT.getEntityProperty());
      assertEquals("transaction.totalInstallment", TagTransactionColumn.TOTAL_INSTALLMENT.getEntityProperty());
    }

    @Test
    @DisplayName("fromString ile enum değeri bulunmalı")
    void fromString_shouldReturnCorrectEnum() {
      assertEquals(TagTransactionColumn.ACCOUNT_NAME, TagTransactionColumn.fromString("accountName"));
      assertEquals(TagTransactionColumn.TOTAL_AMOUNT, TagTransactionColumn.fromString("totalAmount"));
      assertEquals(TagTransactionColumn.TYPE, TagTransactionColumn.fromString("type"));
    }

    @Test
    @DisplayName("fromString geçersiz değer ile hata fırlatmalı")
    void fromString_shouldThrowForInvalidValue() {
      assertThrows(IllegalArgumentException.class, () -> TagTransactionColumn.fromString("xyz"));
    }

    @Test
    @DisplayName("Tüm enum değerleri mevcut olmalı")
    void shouldHaveAllValues() {
      assertEquals(6, TagTransactionColumn.values().length);
    }
  }

  @Nested
  @DisplayName("BaseSortColumn - genel fromString davranışı")
  class BaseSortColumnTests {

    @Test
    @DisplayName("Enum adı ile eşleşmeli (case insensitive)")
    void shouldMatchByEnumName() {
      assertEquals(TransactionSortColumn.DESCRIPTION, BaseSortColumn.fromString(TransactionSortColumn.class, "description"));
      assertEquals(TransactionSortColumn.DESCRIPTION, BaseSortColumn.fromString(TransactionSortColumn.class, "DESCRIPTION"));
    }

    @Test
    @DisplayName("DisplayName ile eşleşmeli")
    void shouldMatchByDisplayName() {
      assertEquals(TransactionSortColumn.CONTACT_NAME, BaseSortColumn.fromString(TransactionSortColumn.class, "contactName"));
      assertEquals(TransactionSortColumn.ACCOUNT_NAME, BaseSortColumn.fromString(TransactionSortColumn.class, "accountName"));
    }

    @Test
    @DisplayName("Alt çizgi olmadan eşleşmeli")
    void shouldMatchWithoutUnderscore() {
      assertEquals(TransactionSortColumn.CONTACT_NAME, BaseSortColumn.fromString(TransactionSortColumn.class, "contactname"));
    }
  }
}
