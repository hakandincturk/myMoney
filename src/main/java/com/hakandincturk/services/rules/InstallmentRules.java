package com.hakandincturk.services.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.models.User;

@Service
public class InstallmentRules {

  @Autowired
  private UserRules userRules;

  public User getValidatedUser(Long userId){
    return userId != null ? userRules.checkUserExistAndGet(userId) : null;
  }
  
}
