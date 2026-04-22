package com.hakandincturk.services.abstracts;

import org.springframework.data.domain.Page;

import com.hakandincturk.dtos.tag.request.FilterListUserTags;
import com.hakandincturk.dtos.tag.response.ListUserTagsDto;
import com.hakandincturk.dtos.tag.response.ListUserTagsWithTransactionCountDto;

public interface TagService {
  public Page<ListUserTagsDto> listUserTags(Long userId, FilterListUserTags body);
  public Page<ListUserTagsWithTransactionCountDto> listUserTagsWithTransactionCount(Long userId, FilterListUserTags body);
  public void deleteTag(Long userId, Long tagId);
}
