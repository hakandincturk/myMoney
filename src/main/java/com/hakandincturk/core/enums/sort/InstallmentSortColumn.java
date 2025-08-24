package com.hakandincturk.core.enums.sort;

import lombok.Getter;

@Getter
public enum InstallmentSortColumn implements BaseSortColumn {
  CONTACT_NAME("transaction.name", "transactionName"),
  DEBT_DATE("debtDate", "debtDate"),
  INSTALLMENT_NUMBER("installmentNumber", "installmentNumber"),
  AMOUNT("amount", "amount"),
  IS_PAID("isPaid", "isPaid"),
  PAID_DATE("paidDate", "paidDate");

  private final String entityProperty;
  private final String displayName;
  
  InstallmentSortColumn(String entityProperty, String displayName) {
    this.entityProperty = entityProperty;
    this.displayName = displayName;
  }
  
  public static InstallmentSortColumn fromString(String columnName) {
    return BaseSortColumn.fromString(InstallmentSortColumn.class, columnName);
  }
  
}
