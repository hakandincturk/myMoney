package com.hakandincturk.services.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.security.services.JwtService;
import com.hakandincturk.services.abstracts.AuthService;

@Service
public class AuthServiceImpl implements AuthService  {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AuthenticationProvider authenticationProvider;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;


  @Override
  public LoginResponseDto login(LoginRequestDto body) {
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(body.getEmail(), body.getPassword());
    authenticationProvider.authenticate(usernamePasswordAuthenticationToken);

    Optional<User> dbUser = userRepository.findByEmailAndIsRemovedFalse(body.getEmail());
    String accessToken = jwtService.generateToken(dbUser.get());

    return new LoginResponseDto(accessToken);
  }


  @Override
  public void register(RegisterRequestDto body) {
    Optional<User> dbUser = userRepository.findByEmailAndIsRemovedFalse(body.getEmail());
    if(dbUser.isPresent()){
      throw new ConflictException("Bu email zaten kullanimda");
    }

    User newUser = new User(
      body.getFullName(),
      body.getEmail(),
      passwordEncoder.encode(body.getPassword()),
      body.getPhone()
    );
    userRepository.save(newUser);
  }
  
}
