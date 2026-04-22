package com.hakandincturk.core.enums.sort;

import lombok.Getter;

@Getter
public enum TagTransactionColumn implements BaseSortColumn {
  ACCOUNT_NAME("transaction.account.name", "accountName"),
  TYPE("transaction.type", "type"),
  STATUS("transaction.status", "status"),
  TOTAL_AMOUNT("transaction.totalAmount", "totalAmount"),
  PAID_AMOUNT("transaction.paidAmount", "paidAmount"),
  TOTAL_INSTALLMENT("transaction.totalInstallment", "totalInstallment");

  private final String entityProperty;
  private final String displayName;

  TagTransactionColumn(String entityProperty, String displayName) {
      this.entityProperty = entityProperty;
      this.displayName = displayName;
  }

  public static TagTransactionColumn fromString(String columnName) {
      return BaseSortColumn.fromString(TagTransactionColumn.class, columnName);
  }
}
