package com.shivamai.otp.account.specification;

import com.shivamai.otp.application.entity.DeveloperApplication;
import com.shivamai.otp.application.enums.ApplicationStatus;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class DeveloperApplicationAdminSpecification {

    private DeveloperApplicationAdminSpecification() {
    }

    public static Specification<DeveloperApplication>
    withFilters(
            String search,
            List<ApplicationStatus> statuses,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        return (root, query, cb) -> {

            var predicate =
                    cb.conjunction();

            if (statuses != null
                    && !statuses.isEmpty()) {

                predicate =
                        cb.and(
                                predicate,
                                root.get("status")
                                        .in(statuses)
                        );
            }

            if (search != null
                    && !search.isBlank()) {

                String value =
                        "%" + search.toLowerCase() + "%";

                predicate =
                        cb.and(
                                predicate,
                                cb.or(

                                        cb.like(
                                                cb.lower(
                                                        root.get("applicationName")
                                                ),
                                                value
                                        ),

                                        cb.like(
                                                cb.lower(
                                                        root.get("clientId")
                                                ),
                                                value
                                        ),

                                        cb.like(
                                                cb.lower(
                                                        root.get("webhookUrl")
                                                ),
                                                value
                                        )
                                )
                        );
            }

            if (fromDate != null) {

                predicate =
                        cb.and(
                                predicate,
                                cb.greaterThanOrEqualTo(
                                        root.get("createdAt"),
                                        fromDate
                                )
                        );
            }

            if (toDate != null) {

                predicate =
                        cb.and(
                                predicate,
                                cb.lessThanOrEqualTo(
                                        root.get("createdAt"),
                                        toDate
                                )
                        );
            }

            return predicate;
        };
    }
}