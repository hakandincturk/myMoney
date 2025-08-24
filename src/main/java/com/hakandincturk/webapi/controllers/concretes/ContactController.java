package com.hakandincturk.webapi.controllers.concretes;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.SortablePageRequest;
import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;

public interface ContactController {
  public ApiResponse<?> createContact(CreateContactRequestDto body);
  public ApiResponse<PagedResponse<ListMyContactsResponseDto>> listMyActiveContacts(SortablePageRequest pageRequest);
  public ApiResponse<?> updateMyAccount(Long contactId,  UpdateMyContactRequestDto body);
  public ApiResponse<?> deleteContact(Long contactId);
}
