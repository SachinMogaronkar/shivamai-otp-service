package com.shivamai.otp.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableFactory {

    private PageableFactory() {
    }

    public static Pageable create(
            PageQuery query
    ) {

        Sort.Direction direction =
                "asc".equalsIgnoreCase(
                        query.getDirection()
                )
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return PageRequest.of(
                query.getPage(),
                query.getSize(),
                Sort.by(
                        direction,
                        query.getSortBy()
                )
        );
    }
}