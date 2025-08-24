package com.hakandincturk.core.enums.sort;

import lombok.Getter;

@Getter
public enum ContactSortColumn implements BaseSortColumn {
  FULL_NAME("fullName", "fullName"),
  EMAIL("email", "email"),
  PHONE("phone", "phone"),
  CREATED_DATE("createdDate", "createdDate"),
  UPDATED_DATE("updatedDate", "updatedDate");
  
  private final String entityProperty;
  private final String displayName;
  
  ContactSortColumn(String entityProperty, String displayName) {
    this.entityProperty = entityProperty;
    this.displayName = displayName;
  }
  
  public static ContactSortColumn fromString(String columnName) {
    return BaseSortColumn.fromString(ContactSortColumn.class, columnName);
  }
}
