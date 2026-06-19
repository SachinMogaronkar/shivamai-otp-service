package com.shivamai.otp.common.pagination;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageQuery {

    private int page;

    private int size;

    private String sortBy;

    private String direction;

    private String search;
}