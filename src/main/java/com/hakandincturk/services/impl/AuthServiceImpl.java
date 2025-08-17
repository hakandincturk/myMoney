package com.hakandincturk.services.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.UnauthorizedException;
import com.hakandincturk.dtos.auth.request.LoginRequestDto;
import com.hakandincturk.dtos.auth.request.RegisterRequestDto;
import com.hakandincturk.dtos.auth.response.LoginResponseDto;
import com.hakandincturk.models.User;
import com.hakandincturk.repositories.UserRepository;
import com.hakandincturk.security.services.JwtService;
import com.hakandincturk.services.abstracts.AuthService;
import com.hakandincturk.services.rules.AuthRules;

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

  @Autowired
  private AuthRules authRules;


  @Override
  public LoginResponseDto login(LoginRequestDto body) throws UnauthorizedException {
    try{
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(body.getEmail(), body.getPassword());
      authenticationProvider.authenticate(usernamePasswordAuthenticationToken);

      Optional<User> dbUser = userRepository.findByEmailAndIsRemovedFalse(body.getEmail());
      String accessToken = jwtService.generateToken(dbUser.get());

      return new LoginResponseDto(accessToken);
    }
    catch(Exception ex){
      throw new UnauthorizedException("Giriş başarısız. Lütfen bilgilerinizi kontrol edin");
    }
  }


  @Override
  public void register(RegisterRequestDto body) {
    authRules.checkUserEmailExist(body.getEmail());

    User newUser = new User(
      body.getFullName(),
      body.getEmail(),
      passwordEncoder.encode(body.getPassword()),
      body.getPhone()
    );
    userRepository.save(newUser);
  }
  
}
