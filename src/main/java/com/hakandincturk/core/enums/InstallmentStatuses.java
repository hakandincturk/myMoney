package com.hakandincturk.core.enums;

public enum InstallmentStatuses {
  ACTIVE(1),   // Aktif taksit - Active installment
  SKIPPED(2);  // Ödenmeyecek taksit - Skipped installment

  private final int value;

  InstallmentStatuses(int value){
    this.value = value;
  }

  public int getValue(){
    return value;
  }

}
