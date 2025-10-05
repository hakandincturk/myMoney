package com.hakandincturk.factories;

import org.springframework.stereotype.Component;

import com.hakandincturk.models.Category;
import com.hakandincturk.models.Users;

@Component
public class CategoryFactory {

  public Category createCategory(String name, Users user){
    Category category = new Category();
    category.setName(name);
    category.setUser(user);
    return category;
  }
  
}
