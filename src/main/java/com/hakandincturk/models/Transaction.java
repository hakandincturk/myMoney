package com.hakandincturk.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity()
@Table(name = "Transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntitiy {
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private User user;

  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Contact contact;

  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Account account;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private TransactionTypes type;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private TransactionStatuses status;
  
  @Column(name = "totalAmount")
  private BigDecimal totalAmomunt;

  @Column(name = "paidAmount")
  private BigDecimal paidAmount;

  @Column(name = "totalInstallment")
  private int totalInstallment;

  @Column(name = "description")
  private String description;

}
