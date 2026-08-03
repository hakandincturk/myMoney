package com.hakandincturk.core.enums;

public enum PeriodKind {
  ACTUAL(1),     // Geçmiş ay - veriler kesinleşmiş
  PARTIAL(2),    // İçinde bulunulan ay - kısmen gerçekleşmiş
  PROJECTED(3);  // Gelecek ay - sadece projeksiyon

  private final int value;

  PeriodKind(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
