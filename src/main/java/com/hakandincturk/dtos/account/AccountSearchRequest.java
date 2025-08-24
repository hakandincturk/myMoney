package com.hakandincturk.dtos.account;

import com.hakandincturk.dtos.SortablePageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountSearchRequest extends SortablePageRequest {
    // Account'a özel arama kriterleri
    private String accountName;
    private String accountType;
    private Boolean isActive;
}
