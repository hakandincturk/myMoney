package com.hakandincturk.factories;

import org.springframework.stereotype.Component;

import com.hakandincturk.models.Category;
import com.hakandincturk.models.User;

@Component
public class CategoryFactory {

  public Category createCategory(String name, User user){
    Category category = new Category();
    category.setName(name);
    category.setUser(user);
    return category;
  }
  
}
