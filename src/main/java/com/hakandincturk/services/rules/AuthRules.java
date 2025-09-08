package com.hakandincturk.services.rules;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthRules {

  private final UserRepository userRepository;

  public void checkUserEmailExist(String email){
    if(userRepository.findByEmailAndIsRemovedFalse(email).isPresent()){
      throw new ConflictException("Bu email zaten kullanimda");
    }
  }
  
}
