package com.hakandincturk.myMoney.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.monthlySummery.request.BackFillMonthlySummeriesRequest;
import com.hakandincturk.services.abstracts.MonthlySummaryService;
import com.hakandincturk.webapi.controllers.impl.MonthlySummaryControllerImpl;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryControllerTest {

  @InjectMocks
  private MonthlySummaryControllerImpl controller;

  @Mock
  private MonthlySummaryService monthlySummaryService;

  @Test
  @DisplayName("Belirli kullanıcılar için backfill - başarılı")
  void fillForUsers_shouldReturnSuccess() {
    BackFillMonthlySummeriesRequest body = new BackFillMonthlySummeriesRequest();
    body.setUserIds(List.of(1L, 2L));

    ApiResponse<?> response = controller.fillForUsers(body);

    assertTrue(response.isType());
    verify(monthlySummaryService).fillForUsers(body);
  }

  @Test
  @DisplayName("Tüm kullanıcılar için backfill - başarılı")
  void fillForAllUsers_shouldReturnSuccess() {
    ApiResponse<?> response = controller.fillForAllUsers();

    assertTrue(response.isType());
    verify(monthlySummaryService).fillForAllUsers();
  }
}
