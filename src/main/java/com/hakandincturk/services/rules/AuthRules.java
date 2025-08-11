package com.hakandincturk.services.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.repositories.UserRepository;

@Service
public class AuthRules {

  @Autowired
  private UserRepository userRepository;

  public void checkUserEmailExist(String email){
    if(userRepository.findByEmailAndIsRemovedFalse(email).isPresent()){
      throw new ConflictException("Bu email zaten kullanimda");
    }
  }
  
}
