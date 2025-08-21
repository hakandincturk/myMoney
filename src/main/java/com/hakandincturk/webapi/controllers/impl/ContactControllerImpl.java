package com.hakandincturk.webapi.controllers.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hakandincturk.core.payload.ApiResponse;
import com.hakandincturk.dtos.contact.request.CreateContactRequestDto;
import com.hakandincturk.dtos.contact.request.UpdateMyContactRequestDto;
import com.hakandincturk.dtos.contact.response.ListMyContactsResponseDto;
import com.hakandincturk.security.JwtAuthentication;
import com.hakandincturk.services.abstracts.ContactService;
import com.hakandincturk.webapi.controllers.BaseController;
import com.hakandincturk.webapi.controllers.concretes.ContactController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/contact")
@Tag(name = "Contact", description = "Kullanıcı işlemleri")
public class ContactControllerImpl extends BaseController implements ContactController {

  @Autowired
  private ContactService contactService;

  @Override
  @PostMapping(value = "/my")
  @Operation(summary = "Create Contact", description = "Kullanıcının yeni kişi oluşturmasını sağlar")
  public ApiResponse<?> createContact(@Valid @RequestBody CreateContactRequestDto body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      contactService.createAccount(userId, body);
      return success("Kişi oluşturuldu", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @GetMapping(value = "/my/active")
  public ApiResponse<List<ListMyContactsResponseDto>> listMyActiveContacts() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      return success("Kişi listeniz getirildi", contactService.listMyActiveContacts(userId));
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @PutMapping(value = "/my/{contactId}")
  public ApiResponse<?> updateMyAccount(@PathVariable(value = "contactId") Long contactId, @RequestBody UpdateMyContactRequestDto body) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      contactService.updateMyContact(userId, contactId, body);
      return success("Kişi başarılı bir şekilde güncellendi", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }

  @Override
  @DeleteMapping(value = "/my/{contactId}")
  public ApiResponse<?> deleteContact(@PathVariable(name = "contactId") Long contactId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if(auth instanceof JwtAuthentication jwtAuth){
      Long userId = jwtAuth.getUserId();
      contactService.deleteContact(userId, contactId);
      return success("Kişi başarılı bir şekilde silindi", null);
    }
    else {
      return error("Kullanıcı verilerine ulaşılamadı");
    }
  }
  
}
