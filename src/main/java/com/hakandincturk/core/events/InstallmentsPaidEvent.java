package com.hakandincturk.core.events;

import java.time.LocalDate;
import java.util.List;

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
public class InstallmentsPaidEvent {
  Users installmentUser;
  List<Installment> installments;
  LocalDate paidDate;
}
