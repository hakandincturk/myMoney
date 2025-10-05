package com.hakandincturk.core.enums;

public enum MonthlySummeryTypes {
  TRANSACTION(1), // ozeti transaction date e gore
  PAYMENT(1); // ozeti payment_date e gore 

  private final int value;

  MonthlySummeryTypes(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }


}
