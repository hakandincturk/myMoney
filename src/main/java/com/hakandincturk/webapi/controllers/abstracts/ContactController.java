package com.hakandincturk.webapi.controllers.abstracts;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.core.payload.PagedResponse;
import com.hakandincturk.dtos.contact.request.ContactFilterRequestDto;
import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;

public interface ContactController {
  ApiResponse<?> createContact(CreateContactRequestDto body);
  ApiResponse<PagedResponse<ListMyContactsResponseDto>> listMyActiveContacts(ContactFilterRequestDto pageRequest);
  ApiResponse<?> updateMyAccount(Long contactId,  UpdateMyContactRequestDto body);
  ApiResponse<?> deleteContact(Long contactId);
}
