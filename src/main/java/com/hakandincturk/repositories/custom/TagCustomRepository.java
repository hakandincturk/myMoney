package com.hakandincturk.repositories.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.hakandincturk.dtos.tag.response.ListUserTagsWithTransactionCountDto;
import com.hakandincturk.models.Tag;

public interface TagCustomRepository {
  Page<ListUserTagsWithTransactionCountDto> findWithTransactionCount(
    Specification<Tag> specs,
    Pageable page
  );
}
