package com.hakandincturk.core.enums.sort;

public interface BaseSortColumn {
  String getEntityProperty();
  String getDisplayName();
  
  static <T extends Enum<T> & BaseSortColumn> T fromString(Class<T> enumClass, String columnName) {
    for (T column : enumClass.getEnumConstants()) {
      if (column.name().equalsIgnoreCase(columnName) || 
        column.name().toLowerCase().replace("_", "").equals(columnName.toLowerCase()) ||
        column.getDisplayName().equalsIgnoreCase(columnName)) {
        return column;
      }
    }
    throw new IllegalArgumentException("Unknown sort column for " + enumClass.getSimpleName() + ": " + columnName);
  }
}
