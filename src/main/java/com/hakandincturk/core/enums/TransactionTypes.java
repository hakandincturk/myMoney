package com.hakandincturk.core.enums;

public enum TransactionTypes {
  DEBT(1),        // Borç oluşturma
  CREDIT(2),      // Alacak oluşturma
  PAYMENT(3),     // Ödeme yapma
  COLLECTION(4);  // Tahsilat yapma

  private final int value;

  TransactionTypes(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}