package com.hakandincturk.services.abstracts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;

public interface ContactService {
  public void createAccount(Long userId, CreateContactRequestDto body);
  public Page<ListMyContactsResponseDto> listMyActiveContacts(Long userId, Pageable pageData);
  public void updateMyContact(Long userId, Long contactId, UpdateMyContactRequestDto body);
  public void deleteContact(Long userId, Long contactId);
}
