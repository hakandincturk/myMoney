package com.hakandincturk.myMoney.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.utils.TagNameUtils;

class TagNameUtilsTest {

  @Test
  @DisplayName("Görünen ad kırpılmalı, ardışık boşluklar teke inmeli, harfler korunmalı")
  void clean_shouldTrimAndCollapseWhitespace() {
    assertEquals("Aylık Abonelik", TagNameUtils.clean("  Aylık   Abonelik "));
    assertEquals("", TagNameUtils.clean(null));
  }

  @Test
  @DisplayName("Karşılaştırma anahtarı Türkçe kurallarla küçültülmeli")
  void normalize_shouldLowerCaseWithTurkishRules() {
    assertEquals("aylık abonelik", TagNameUtils.normalize(" AYLIK  Abonelik"));
    assertEquals("işlem", TagNameUtils.normalize("İŞLEM"));
    assertEquals(TagNameUtils.normalize("aylık abonelik"), TagNameUtils.normalize("Aylık Abonelik "));
  }
}
