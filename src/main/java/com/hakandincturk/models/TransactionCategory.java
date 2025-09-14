package com.hakandincturk.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TransactionCategory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategory extends BaseEntitiy {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id")
  private Transaction transaction;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;
  
}
