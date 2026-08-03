package com.hakandincturk.core.enums;

public enum RecurringKinds {
  INSTALLMENT(1), // Çok taksitli tek bir işlem - tanım gereği tekrar eder
  REPEATED(2);    // Aynı isimle farklı aylarda tekrar eden ayrı işlemler

  private final int value;

  RecurringKinds(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
