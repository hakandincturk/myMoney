package com.hakandincturk.core.enums;

public enum TransactionStatuses {
  PENDING(1), 
  PARTIAL(2),
  PAID(3);
  

  private final int value;

  TransactionStatuses(int value){
    this.value = value;
  }

  public int getValue(){
    return value;
  }
  
}
