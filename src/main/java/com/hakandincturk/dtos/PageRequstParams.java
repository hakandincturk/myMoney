package com.hakandincturk.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequstParams {
    private int pageNumber = 0;
    private int pageSize = 10;
}
