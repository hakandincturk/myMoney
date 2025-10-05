package com.hakandincturk.services.rules;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Users;
import com.hakandincturk.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRules {

  private final UserRepository userRepository;

  public Users checkUserExistAndGet(Long userId){
    Optional<Users> dbUser = userRepository.findByIdAndIsRemovedFalse(userId);
    if(dbUser.isEmpty()){
      throw new NotFoundException("Kullanıcı bilgileri bulunamadı");
    }

    return dbUser.get();
  }

  public List<Users> checkAllUsersExistAndGet(List<Long> userIds){
    List<Users> dbUsers = userRepository.findByIdInAndIsRemovedFalse(userIds);
    if(dbUsers.size() != userIds.size()){
      throw new NotFoundException("Kullanıcı bilgileri bulunamadı");
    }

    return dbUsers;
  }
  
}
