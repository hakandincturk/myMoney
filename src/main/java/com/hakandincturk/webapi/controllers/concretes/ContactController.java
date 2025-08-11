package com.hakandincturk.webapi.controllers.concretes;

import java.util.List;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;

public interface ContactController {
  public ApiResponse<?> createContact(CreateContactRequestDto body);
  public ApiResponse<List<ListMyContactsResponseDto>> listMyActiveContacts();
  public ApiResponse<?> updateMyAccount(Long contactId,  UpdateMyContactRequestDto body);
  public ApiResponse<?> deleteContact(Long contactId);
}
