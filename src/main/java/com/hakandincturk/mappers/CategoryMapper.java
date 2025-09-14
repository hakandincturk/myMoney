package com.hakandincturk.mappers;

import org.mapstruct.Mapper;

import com.hakandincturk.dtos.category.response.ListUserCategoriesDto;
import com.hakandincturk.models.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
  ListUserCategoriesDto toListUserCategoriesDto(Category category); 
}
