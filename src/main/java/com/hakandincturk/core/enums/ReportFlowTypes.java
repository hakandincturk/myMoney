package com.hakandincturk.core.enums;

public enum ReportFlowTypes {
  EXPENSE(1), // Gider akışı - DEBT ve PAYMENT tipindeki hareketler
  INCOME(2);  // Gelir akışı - CREDIT ve COLLECTION tipindeki hareketler

  private final int value;

  ReportFlowTypes(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
