package com.hakandincturk.core.enums;

public enum DashboardCategorySummaryTypes {
  MONTHLY(1),
  YEARLY(2);

  private final int value;
  
  DashboardCategorySummaryTypes(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
