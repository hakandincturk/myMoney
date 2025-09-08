package com.hakandincturk.services.rules;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRules {

  private final UserRepository userRepository;

  public User checkUserExistAndGet(Long userId){
    Optional<User> dbUser = userRepository.findByIdAndIsRemovedFalse(userId);
    if(dbUser.isEmpty()){
      throw new NotFoundException("Kullanıcı bilgileri bulunamadı");
    }

    return dbUser.get();
  }
  
}
