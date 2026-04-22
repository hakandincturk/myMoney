package com.hakandincturk.core.enums;

public enum DashboardTagSummaryTypes {
  MONTHLY(1),
  YEARLY(2);

  private final int value;

  DashboardTagSummaryTypes(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
