package com.hakandincturk.core.enums;

public enum Times {
  THIRTY_MINUTES(30 * 60 * 1000L),
  ONE_HOUR(60 * 60 * 1000L),
  THREE_HOURS(3 * 60 * 60 * 1000L),
  SIX_HOURS(6 * 60 * 60 * 1000L),
  TWELVE_HOURS(12 * 60 * 60 * 1000L),
  ONE_DAY(24 * 60 * 60 * 1000L);

  private final long milliseconds;

  Times(long milliseconds) {
      this.milliseconds = milliseconds;
  }

  public long getMilliseconds() {
      return milliseconds;
  }
}
