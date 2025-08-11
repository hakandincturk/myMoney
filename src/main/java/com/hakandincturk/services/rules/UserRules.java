package com.hakandincturk.services.rules;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.UserRepository;

@Service
public class UserRules {

  @Autowired
  private UserRepository userRepository;

  public User checkUserExistAndGet(Long userId){
    Optional<User> dbUser = userRepository.findByIdAndIsRemovedFalse(userId);
    if(dbUser.isEmpty()){
      throw new NotFoundException("Kullanıcı bilgileri bulunamadı");
    }

    return dbUser.get();
  }
  
}
