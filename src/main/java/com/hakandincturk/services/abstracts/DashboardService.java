package com.hakandincturk.services.abstracts;

import com.hakandincturk.dtos.dashboard.response.QuickViewResponseDto;

public interface DashboardService {
  QuickViewResponseDto quickViewResponse(Long userId);
}
