package com.hakandincturk.models;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class BaseEntitiy {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private boolean isRemoved = false;

  @Column(name = "createdAt")
  @DateTimeFormat(iso = ISO.DATE)
  private Date createdAt;

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = new Date();
    }
  }
}
