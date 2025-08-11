package com.hakandincturk.core.enums;

public enum AccountTypes {
  CREDIT_CARD(1),
  CASH(2),
  BANK(3);

  private final int value;

  AccountTypes(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
