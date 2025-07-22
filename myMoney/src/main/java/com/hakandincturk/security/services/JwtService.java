package com.hakandincturk.security.services;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hakandincturk.core.enums.Times;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  @Value("${jwt.secret.key}")
  public String SECRET_KEY;


  public Key getKey(){
    byte[] bytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(bytes);
  }

  public String generateToken(UserDetails userDetail){
    return Jwts.builder()
            .setSubject(userDetail.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + Times.ONE_DAY.getMilliseconds()))
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  public Claims getClaims(String token){
    Claims claims = Jwts.parserBuilder()
                      .setSigningKey(getKey())
                      .build()
                      .parseClaimsJws(token)
                      .getBody();
    return claims;
  }

  public <T> T exportToken(String token, Function<Claims, T> claimFunction){
    Claims claims = getClaims(token);
    return claimFunction.apply(claims);
  }

  public String getEmailByToken(String token){
    return exportToken(token, Claims::getSubject);
  }

  public boolean isTokenDateValid(String token){
    Date tokenExpireDate = exportToken(token, Claims::getExpiration);
    return new Date().before(tokenExpireDate);
  }

  public boolean isTokenValid(String token, UserDetails userDetails){
    String email = getEmailByToken(token);
    boolean isTokenDateValid = isTokenDateValid(token);
    return email.equals(userDetails.getUsername()) && isTokenDateValid;
  }

  

  
}
