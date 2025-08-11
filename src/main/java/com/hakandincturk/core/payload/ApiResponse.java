package com.hakandincturk.core.payload;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private boolean type;

  private String message;

  private LocalDateTime timestamp;

  private T data;

  // public static <T> ApiResponse<T> success(String message) {
  //   return new ApiResponse<>(true, message, LocalDateTime.now(), null);
  // }

  // public static <T> ApiResponse<T> success(String message, T data) {
  //   return new ApiResponse<>(true, message, LocalDateTime.now(), data);
  // }

  // public static <T> ApiResponse<T> error(String message) {
  //   return new ApiResponse<>(false, message, LocalDateTime.now(), null);
  // }

  // public static <T> ApiResponse<T> error(String message, T data) {
  //   return new ApiResponse<>(false, message, LocalDateTime.now(), data);
  // }
  
}
