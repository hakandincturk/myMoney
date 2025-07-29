package com.hakandincturk.core.handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.hakandincturk.core.exception.BusinessException;
import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.core.exception.UnauthorizedException;
import com.hakandincturk.core.payload.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

  // @ExceptionHandler(value = {Exception.class})
  // public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception ex, WebRequest request) {
  //   Map<String, String> errorDetails = new HashMap<>();
  //   errorDetails.put("path", request.getDescription(false).substring(4));
  //   return ResponseEntity.internalServerError().body(createApiResponse(ex.getMessage(), errorDetails));
  // }

  // @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  // public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request){

  //   Map<String, List<String>> errorMap = new HashMap<>();
  //   for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {
  //     String fieldName = ((FieldError)objectError).getField();
  //     if(errorMap.containsKey(fieldName)){
  //       errorMap.put(fieldName, addErrorToList(errorMap.get(fieldName), objectError.getDefaultMessage()));
  //     }
  //     else{
  //       errorMap.put(fieldName, addErrorToList(new ArrayList<>(), objectError.getDefaultMessage()));
  //     }
  //   }

  //   return ResponseEntity.badRequest().body(createApiResponse(ex.getMessage(), errorMap));
  // }

  // @ExceptionHandler(value = {UnauthorizedException.class})
  // public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(UnauthorizedException ex, WebRequest request){
  //   Map<String, String> errorDetails = new HashMap<>();
  //   errorDetails.put("path", request.getDescription(false).substring(4));
  //   errorDetails.put("token", request.getHeader("Authorization"));
  //   return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createApiResponse(ex.getMessage(), errorDetails));
  // }

  // @ExceptionHandler(value = {NotFoundException.class})
  // public ResponseEntity<ApiResponse<?>> handleNotFoundException(NotFoundException ex, WebRequest request){
  //   Map<String, String> errorDetails = new HashMap<>();
  //   errorDetails.put("path", request.getDescription(false).substring(4));
  //   return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createApiResponse(ex.getMessage(), errorDetails));
  // }

  // @ExceptionHandler(value = {ConflictException.class})
  // public ResponseEntity<ApiResponse<?>> handleConflictException(ConflictException ex, WebRequest request){
  //   Map<String, String> errorDetails = new HashMap<>();
  //   errorDetails.put("path", request.getDescription(false).substring(4));
  //   return ResponseEntity.status(HttpStatus.CONFLICT).body(createApiResponse(ex.getMessage(), errorDetails));
  // }

  // @ExceptionHandler(value = {BusinessException.class})
  // public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex, WebRequest request){
  //   Map<String, String> errorDetails = new HashMap<>();
  //   errorDetails.put("path", request.getDescription(false).substring(4));
  //   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createApiResponse(ex.getMessage(), errorDetails));
  // }

  private List<String> addErrorToList(List<String> list, String errorMessage){
    list.add(errorMessage);
    return list;
  }

  private <T> ApiResponse<T> createApiResponse(String message, T data){
    ApiResponse<T> apiResponse = new ApiResponse<>(false, message, LocalDateTime.now(), data);
    return apiResponse;
  }
  
}
