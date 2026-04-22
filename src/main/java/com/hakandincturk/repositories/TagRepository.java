package com.hakandincturk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hakandincturk.models.Tag;
import com.hakandincturk.repositories.custom.TagCustomRepository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag>, TagCustomRepository {
  Optional<Tag> findByIdAndIsRemovedFalse(Long id);
  Optional<Tag> findByIdAndUserIdAndIsRemovedFalse(Long id, Long userId);
  List<Tag> findAllByIdInAndIsRemovedFalse(List<Long> ids);
}
