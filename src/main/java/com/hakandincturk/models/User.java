package com.hakandincturk.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"User\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntitiy implements UserDetails {

  @Column(name = "fullName")
  private String fullName;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "phone")
  private String phone;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Category> categories;

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<>();
  }
  
}
