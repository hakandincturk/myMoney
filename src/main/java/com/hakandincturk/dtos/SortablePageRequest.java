package com.hakandincturk.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortablePageRequest extends PageRequstParams {
    private String columnName;
    private boolean asc = true;   
}
