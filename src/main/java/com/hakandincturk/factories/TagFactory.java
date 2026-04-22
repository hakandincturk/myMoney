package com.hakandincturk.factories;

import org.springframework.stereotype.Component;

import com.hakandincturk.models.Tag;
import com.hakandincturk.models.Users;

@Component
public class TagFactory {

  public Tag createTag(String name, Users user){
    Tag tag = new Tag();
    tag.setName(name);
    tag.setUser(user);
    return tag;
  }

}
