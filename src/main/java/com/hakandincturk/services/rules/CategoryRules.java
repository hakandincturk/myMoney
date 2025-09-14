package com.hakandincturk.services.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hakandincturk.core.exception.NotFoundException;
import com.hakandincturk.models.Category;
import com.hakandincturk.repositories.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryRules {

  private final CategoryRepository categoryRepository;

  public Category checkCategoryExistAndGet(Long id){
    Optional<Category> dbCategory = categoryRepository.findByIdAndIsRemovedFalse(id);
    if(dbCategory.isEmpty()){
      throw new NotFoundException("Kategori bulunamadı");
    }
    return dbCategory.get();
  }

  public List<Category> checkAllIdsAndGet(List<Long> ids){
    if(ids == null || ids.isEmpty()){
        return new ArrayList<>(); // Boş liste döndür, hata fırlatma
    }

    List<Category> dbCategories = categoryRepository.findAllByIdInAndIsRemovedFalse(ids);
    if(dbCategories.size() != ids.size()){
      throw new NotFoundException("Aranan kategorilerin hepsi bulunmadi");
    }

    return dbCategories;
  }
}
