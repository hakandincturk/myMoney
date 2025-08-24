package com.hakandincturk.core.enums.sort;

import lombok.Getter;

@Getter
public enum TransactionSortColumn implements BaseSortColumn {
  CONTACT_NAME("contact.fullName", "contactName"),
  CATEGORY_NAME("category.name", "categoryName"),
  ACCOUNT_NAME("account.name", "accountName"),
  TRANSACTION_DATE("date", "transactionDate"),
  TRANSACTION_AMOUNT("amount", "transactionAmount"),
  DESCRIPTION("description", "description"),
  CREATED_DATE("createdDate", "createdDate"),
  UPDATED_DATE("updatedDate", "updatedDate");
  
  private final String entityProperty;
  private final String displayName;
  
  TransactionSortColumn(String entityProperty, String displayName) {
    this.entityProperty = entityProperty;
    this.displayName = displayName;
  }
  
  public static TransactionSortColumn fromString(String columnName) {
    return BaseSortColumn.fromString(TransactionSortColumn.class, columnName);
  }
}