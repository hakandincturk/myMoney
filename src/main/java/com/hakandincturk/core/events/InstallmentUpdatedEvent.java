package com.hakandincturk.core.events;

import com.hakandincturk.models.Installment;
import com.hakandincturk.models.Users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentUpdatedEvent {
  private Users user;
  private Installment installment;
}
