package com.hakandincturk.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.hakandincturk.core.enums.PeriodKind;

import lombok.experimental.UtilityClass;

/**
 * Rapor modülünde dönem (yıl-ay) ile ilgili ortak hesaplamalar.
 */
@UtilityClass
public class ReportPeriodUtils {

  /**
   * Dönemin kesinlik seviyesi: geçmiş ay ACTUAL, içinde bulunulan ay PARTIAL, gelecek ay PROJECTED.
   */
  public static PeriodKind kindOf(YearMonth period, LocalDate today){
    YearMonth currentPeriod = YearMonth.from(today);
    if(period.isBefore(currentPeriod)){
      return PeriodKind.ACTUAL;
    }
    if(period.isAfter(currentPeriod)){
      return PeriodKind.PROJECTED;
    }

    return PeriodKind.PARTIAL;
  }

  /**
   * Frontend i18n anahtarı olarak kullanıldığı için ay adı her zaman İngilizce ve büyük harftir.
   */
  public static String labelOf(YearMonth period){
    return period.getMonth().name();
  }

  /**
   * Aralıktaki tüm ayları (her iki uç dahil) kronolojik sırada üretir.
   * Veri olmayan aylar da listelendiği için grafik serisinde delik oluşmaz.
   */
  public static List<YearMonth> monthsBetween(YearMonth start, YearMonth end){
    List<YearMonth> months = new ArrayList<>();
    YearMonth cursor = start;
    while(!cursor.isAfter(end)){
      months.add(cursor);
      cursor = cursor.plusMonths(1);
    }

    return months;
  }

  /**
   * Ortalama günlük gider hesabında kullanılacak gün sayısı.
   * İçinde bulunulan ayda henüz yaşanmamış günler paydayı şişirmesin diye bugüne kadarki gün sayısı alınır.
   */
  public static int divisorDayCount(YearMonth period, LocalDate today){
    if(period.equals(YearMonth.from(today))){
      return today.getDayOfMonth();
    }

    return period.lengthOfMonth();
  }

  /**
   * Opsiyonel sayısal parametreleri normalize eder.
   * Null veya alt sınırın altındaki değer default'a döner, üst sınırı aşan değer sınıra çekilir;
   * bu parametreler için hata dönülmez.
   */
  public static int normalize(Integer value, int defaultValue, int minValue, int maxValue){
    if(value == null || value < minValue){
      return defaultValue;
    }

    return Math.min(value, maxValue);
  }
}
