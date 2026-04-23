package com.hakandincturk.myMoney.factory;

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
  @DisplayName("Tag null isim ile oluşturulabilmeli")
  void createTag_shouldHandleNullName() {
    Users user = new Users();
    user.setId(1L);

    Tag result = tagFactory.createTag(null, user);

    assertNotNull(result);
    assertNull(result.getName());
    assertEquals(user, result.getUser());
  }
}
