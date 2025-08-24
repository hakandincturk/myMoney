package com.hakandincturk.core.enums.sort;

import lombok.Getter;

@Getter
public enum AccountSortColumn implements BaseSortColumn {
  ACCOUNT_NAME("name", "accountName"),
    ACCOUNT_BALANCE("balance", "accountBalance"),
    ACCOUNT_TYPE("type", "accountType"),
    IS_ACTIVE("isActive", "isActive"),
    CREATED_DATE("createdDate", "createdDate"),
    UPDATED_DATE("updatedDate", "updatedDate");
    
    private final String entityProperty;
    private final String displayName;
    
    AccountSortColumn(String entityProperty, String displayName) {
        this.entityProperty = entityProperty;
        this.displayName = displayName;
    }
    
    public static AccountSortColumn fromString(String columnName) {
        return BaseSortColumn.fromString(AccountSortColumn.class, columnName);
    }
}
