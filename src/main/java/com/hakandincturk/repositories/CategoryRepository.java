package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
  Optional<Category> findByIdAndIsRemovedFalse(Long id);
  List<Category> findAllByIdInAndIsRemovedFalse(List<Long> ids);
}
