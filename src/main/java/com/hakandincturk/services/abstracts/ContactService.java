package com.hakandincturk.services.abstracts;

import java.util.List;

import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;

public interface ContactService {
  public void createAccount(Long userId, CreateContactRequestDto body);
  public List<ListMyContactsResponseDto> listMyActiveContacts(Long userId);
  public void updateMyContact(Long userId, Long contactId, UpdateMyContactRequestDto body);
  public void deleteContact(Long userId, Long contactId);
}
