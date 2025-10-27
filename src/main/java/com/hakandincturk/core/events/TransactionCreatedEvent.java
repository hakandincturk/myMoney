package com.hakandincturk.core.events;

import com.hakandincturk.models.Transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
  Transaction transaction;
}
