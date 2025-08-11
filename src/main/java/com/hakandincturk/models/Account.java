package com.hakandincturk.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hakandincturk.core.enums.AccountTypes;
import com.hakandincturk.core.enums.CurrencyTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity()
@Table(name = "Account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntitiy {
  
  @Column(name = "name")
  private String name;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private AccountTypes type;

  @Column(name = "currency")
  @Enumerated(EnumType.STRING)
  private CurrencyTypes currency;

  @Column(name = "totalBalance")
  private BigDecimal totalBalance;

  @Column(name = "balance")
  private BigDecimal balance;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private User user;

}
