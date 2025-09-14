package com.hakandincturk.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hakandincturk.core.enums.TransactionStatuses;
import com.hakandincturk.core.enums.TransactionTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

  @Column(name = "name")
  private String name;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private User user;

  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @JoinColumn(name = "contact_id", nullable = true, unique = false)
  private Contact contact;

  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @JoinColumn(name = "account_id", nullable = false, unique = false)
  private Account account;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private TransactionTypes type;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private TransactionStatuses status;
  
  @Column(name = "totalAmount")
  private BigDecimal totalAmount;

  @Column(name = "paidAmount")
  private BigDecimal paidAmount;

  @Column(name = "totalInstallment")
  private int totalInstallment;

  @Column(name = "description")
  private String description;

  @Column(name = "debtDate")
  private LocalDate debtDate;

  @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<Installment> installments;

  @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<TransactionCategory> transactionCategories;

}
