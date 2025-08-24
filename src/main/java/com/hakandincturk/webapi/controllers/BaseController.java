package com.hakandincturk.webapi.controllers;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.utils.PaginationUtils;

public class BaseController {
  protected <T> ApiResponse<T> success(String message){
    return new ApiResponse<T>(true, message, LocalDateTime.now(), null);
  }

  protected <T> ApiResponse<T> success(String message, T payload){
    return new ApiResponse<T>(true, message, LocalDateTime.now(), payload);
  }

  protected <T> ApiResponse<PagedResponse<T>> successPaged(String message, Page<T> payload){
    return new ApiResponse<PagedResponse<T>>(true, message, LocalDateTime.now(), PaginationUtils.toPagedResponse(payload));
  }

  protected <T> ApiResponse<T> error(String message){
    return new ApiResponse<T>(false, message, LocalDateTime.now(), null);
  }

  protected <T> ApiResponse<T> error(String message, T payload){
    return new ApiResponse<T>(false, message, LocalDateTime.now(), payload);

  }

}
