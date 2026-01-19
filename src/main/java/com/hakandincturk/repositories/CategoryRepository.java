package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Category;
import com.hakandincturk.repositories.custom.CategoryCustomRepository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category>, CategoryCustomRepository {
  Optional<Category> findByIdAndIsRemovedFalse(Long id);
  List<Category> findAllByIdInAndIsRemovedFalse(List<Long> ids);
}
