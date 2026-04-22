package com.hakandincturk.core.enums;

public enum DashboardTagSummarySumMode {
  DOUBLE_COUNT(1), // taksiti her etikete dağıtarak
  DISTRIBUTED(2); // taksit tutarını etiketlere eşit dağıtarak

  private final int value;

  DashboardTagSummarySumMode(int value) {
    this.value = value;
  }

  public int getValue(){
    return this.value;
  }

}
