package com.hakandincturk.webapi.controllers;

import java.time.LocalDateTime;

import com.hakandincturk.core.payload.ApiResponse;

public class BaseController {
  protected <T> ApiResponse<T> success(String message){
    return new ApiResponse<T>(true, message, LocalDateTime.now(), null);
  }

  protected <T> ApiResponse<T> success(String message, T payload){
    return new ApiResponse<T>(true, message, LocalDateTime.now(), payload);

  }

  protected <T> ApiResponse<T> error(String message){
    return new ApiResponse<T>(false, message, LocalDateTime.now(), null);
  }

  protected <T> ApiResponse<T> error(String message, T payload){
    return new ApiResponse<T>(false, message, LocalDateTime.now(), payload);

  }

}
