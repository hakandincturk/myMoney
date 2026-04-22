package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.tag.request.FilterListUserTags;
import com.hakandincturk.dtos.tag.response.ListUserTagsDto;
import com.hakandincturk.dtos.tag.response.ListUserTagsWithTransactionCountDto;

public interface TagController {
  ApiResponse<PagedResponse<ListUserTagsDto>> listUserTags(FilterListUserTags body);
  ApiResponse<PagedResponse<ListUserTagsWithTransactionCountDto>> listUserTagsWithTransactionCount(FilterListUserTags body);
  ApiResponse<?> deleteTag(Long tagId);
}
