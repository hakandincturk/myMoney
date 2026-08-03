package com.hakandincturk.services.rules;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.ValidationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportRules {

  // Uygulamanın anlamlı veri tutabileceği yıl aralığı
  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2100;

  /**
   * Dönem parametrelerini doğrular.
   * Web katmanındaki bean validation'dan bağımsız olarak servis katmanı da kendi kuralını uygular.
   * @param year Rapor yılı
   * @param month Rapor ayı
  */
  public void checkPeriodIsValid(Integer year, Integer month){
    if(year == null || month == null){
      throw new ValidationException("Yıl ve ay bilgisi zorunludur");
    }

    if(month < 1 || month > 12){
      throw new ValidationException("Ay bilgisi 1 ile 12 arasında olmalıdır");
    }

    if(year < MIN_YEAR || year > MAX_YEAR){
      throw new ValidationException("Yıl bilgisi " + MIN_YEAR + " ile " + MAX_YEAR + " arasında olmalıdır");
    }
  }

}
