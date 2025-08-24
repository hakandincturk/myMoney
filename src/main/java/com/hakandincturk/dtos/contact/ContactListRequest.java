package com.hakandincturk.dtos.contact;

import com.hakandincturk.dtos.PageRequstParams;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactListRequest extends PageRequstParams {
    // Contact listesi için sadece pagination, sorting gerekmez
    private String searchTerm;
}
