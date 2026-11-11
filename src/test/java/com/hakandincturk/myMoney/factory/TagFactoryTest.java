package com.hakandincturk.myMoney.factory;

import java.util.List;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hakandincturk.factories.TagFactory;
import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Users;

class TagFactoryTest {

  private final TagFactory tagFactory = new TagFactory();

  @Test
  @DisplayName("Tag başarıyla oluşturulmalı")
  void createTag_shouldCreateTagWithNameAndUser() {
    Users user = new Users();
    user.setId(1L);
    String tagName = "Yemek";

    Tag result = tagFactory.createTag(tagName, user);

    assertNotNull(result);
    assertEquals(tagName, result.getName());
    assertEquals(user, result.getUser());
  }

  @Test
  @DisplayName("Tag null isim ile boş adla oluşturulabilmeli")
  void createTag_shouldHandleNullName() {
    Users user = new Users();
    user.setId(1L);

    Tag result = tagFactory.createTag(null, user);

    assertNotNull(result);
    assertEquals("", result.getName());
    assertEquals(user, result.getUser());
  }

  @Test
  @DisplayName("Tag adı kaydedilmeden önce temizlenmeli")
  void createTag_shouldCleanName() {
    Tag result = tagFactory.createTag("  Aylık   Abonelik ", new Users());

    assertEquals("Aylık Abonelik", result.getName());
  }

  @Test
  @DisplayName("Aynı isimde mevcut etiket varsa yenisi açılmamalı, mevcut etiket dönmeli")
  void resolveTagsByName_shouldReuseExistingTag_ignoringCaseAndWhitespace() {
    Users user = new Users();
    Tag existing = tag(7L, "aylık abonelik");

    List<Tag> result = tagFactory.resolveTagsByName(List.of(" Aylık  Abonelik "), List.of(existing), user);

    assertEquals(1, result.size());
    assertSame(existing, result.get(0));
  }

  @Test
  @DisplayName("Aynı istekte tekrarlanan adlar tek yeni etikete inmeli, boş adlar atlanmalı")
  void resolveTagsByName_shouldCollapseRepeatedNamesAndSkipBlank() {
    Users user = new Users();

    List<Tag> result = tagFactory.resolveTagsByName(Arrays.asList("Market", "market ", "  ", null, "Ev"), List.of(), user);

    assertEquals(2, result.size());
    assertEquals("Market", result.get(0).getName());
    assertNull(result.get(0).getId());
    assertEquals(user, result.get(0).getUser());
    assertEquals("Ev", result.get(1).getName());
  }

  @Test
  @DisplayName("Ad listesi boşsa boş liste dönmeli")
  void resolveTagsByName_shouldReturnEmptyList_whenNamesEmpty() {
    assertTrue(tagFactory.resolveTagsByName(null, List.of(), new Users()).isEmpty());
    assertTrue(tagFactory.resolveTagsByName(List.of(), List.of(), new Users()).isEmpty());
  }

  private Tag tag(Long id, String name) {
    Tag tag = new Tag();
    tag.setId(id);
    tag.setName(name);
    return tag;
  }
}
