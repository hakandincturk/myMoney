package com.hakandincturk.myMoney.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import com.hakandincturk.core.exception.BusinessException;
import com.hakandincturk.core.exception.ConflictException;
import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.core.exception.UnauthorizedException;
import com.hakandincturk.core.exception.ValidationException;
import com.hakandincturk.core.handler.GlobalExceptionHandler;
import com.hakandincturk.core.payload.ApiResponse;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @InjectMocks
  private GlobalExceptionHandler handler;

  @Mock
  private WebRequest webRequest;

  private void setupWebRequest() {
    when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
  }

  @Test
  @DisplayName("NotFoundException 404 döndürmeli")
  void handleNotFoundException_shouldReturn404() {
    setupWebRequest();
    NotFoundException ex = new NotFoundException("Kayıt bulunamadı");

    ResponseEntity<ApiResponse<?>> response = handler.handleNotFoundException(ex, webRequest);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertFalse(response.getBody().isType());
    assertEquals("Kayıt bulunamadı", response.getBody().getMessage());
  }

  @Test
  @DisplayName("ConflictException 409 döndürmeli")
  void handleConflictException_shouldReturn409() {
    setupWebRequest();
    ConflictException ex = new ConflictException("Çakışma");

    ResponseEntity<ApiResponse<?>> response = handler.handleConflictException(ex, webRequest);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("Çakışma", response.getBody().getMessage());
  }

  @Test
  @DisplayName("BusinessException 500 döndürmeli")
  void handleBusinessException_shouldReturn500() {
    setupWebRequest();
    BusinessException ex = new BusinessException("İş kuralı hatası");

    ResponseEntity<ApiResponse<?>> response = handler.handleBusinessException(ex, webRequest);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("İş kuralı hatası", response.getBody().getMessage());
  }

  @Test
  @DisplayName("ValidationException 400 döndürmeli")
  void handleValidationException_shouldReturn400() {
    setupWebRequest();
    ValidationException ex = new ValidationException("Geçersiz değer");

    ResponseEntity<ApiResponse<?>> response = handler.handleBusinessException(ex, webRequest);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Geçersiz değer", response.getBody().getMessage());
  }

  @Test
  @DisplayName("UnauthorizedException 401 döndürmeli")
  void handleUnauthorizedException_shouldReturn401() {
    setupWebRequest();
    when(webRequest.getHeader("Authorization")).thenReturn("Bearer token123");
    UnauthorizedException ex = new UnauthorizedException("Yetkisiz erişim");

    ResponseEntity<ApiResponse<?>> response = handler.handleUnauthorizedException(ex, webRequest);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Yetkisiz erişim", response.getBody().getMessage());
  }

  @Test
  @DisplayName("UnauthorizedException token olmadan 401 döndürmeli")
  void handleUnauthorizedException_withoutToken_shouldReturn401() {
    setupWebRequest();
    when(webRequest.getHeader("Authorization")).thenReturn(null);
    UnauthorizedException ex = new UnauthorizedException("Yetkisiz");

    ResponseEntity<ApiResponse<?>> response = handler.handleUnauthorizedException(ex, webRequest);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  @DisplayName("Genel Exception 500 döndürmeli")
  void handleAllExceptions_shouldReturn500() {
    setupWebRequest();
    Exception ex = new RuntimeException("Beklenmeyen hata");

    ResponseEntity<ApiResponse<?>> response = handler.handleAllExceptions(ex, webRequest);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Beklenmeyen hata", response.getBody().getMessage());
  }
}
