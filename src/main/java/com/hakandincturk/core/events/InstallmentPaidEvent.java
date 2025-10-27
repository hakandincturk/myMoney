package com.hakandincturk.core.events;

import com.hakandincturk.models.Users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaidEvent {
  private Users user;
  private int year;
  private int month;
}
