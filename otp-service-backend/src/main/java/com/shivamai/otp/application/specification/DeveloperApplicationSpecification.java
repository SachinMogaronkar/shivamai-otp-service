package com.shivamai.otp.application.specification;

import com.shivamai.otp.application.entity.DeveloperApplication;
import com.shivamai.otp.application.enums.ApplicationStatus;

import org.springframework.data.jpa.domain.Specification;

public class DeveloperApplicationSpecification {

    private DeveloperApplicationSpecification() {
    }

    public static Specification<DeveloperApplication>
    withFilters(
            Long developerId,
            String search,
            ApplicationStatus status
    ) {

        return (root, query, cb) -> {

            var predicate =
                    cb.equal(
                            root.get("developer").get("id"),
                            developerId
                    );

            if (status != null) {

                predicate =
                        cb.and(
                                predicate,
                                cb.equal(
                                        root.get("status"),
                                        status
                                )
                        );
            }

            if (search != null
                    && !search.isBlank()) {

                predicate =
                        cb.and(
                                predicate,
                                cb.or(
                                        cb.like(
                                                cb.lower(
                                                        root.get("applicationName")
                                                ),
                                                "%" + search.toLowerCase() + "%"
                                        ),

                                        cb.like(
                                                cb.lower(
                                                        root.get("clientId")
                                                ),
                                                "%" + search.toLowerCase() + "%"
                                        )
                                )
                        );
            }

            return predicate;
        };
    }
}