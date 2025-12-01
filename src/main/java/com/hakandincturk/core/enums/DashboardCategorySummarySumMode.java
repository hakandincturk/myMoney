package com.hakandincturk.core.enums;

public enum DashboardCategorySummarySumMode {
  DOUBLE_COUNT(1), // taksiti her kategoriye dağıtarak
  DISTRIBUTED(2); // taksit tutarını kategorilere eşit dağıtarak

  private final int value;

  DashboardCategorySummarySumMode(int value) {
    this.value = value;
  }

  public int getValue(){
    return this.value;
  }

}
