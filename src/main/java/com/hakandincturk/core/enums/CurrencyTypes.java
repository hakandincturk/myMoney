package com.hakandincturk.core.enums;

public enum CurrencyTypes {
  TL(1),
  USD(2),
  EUR(3);

  private final int value;

  CurrencyTypes(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
