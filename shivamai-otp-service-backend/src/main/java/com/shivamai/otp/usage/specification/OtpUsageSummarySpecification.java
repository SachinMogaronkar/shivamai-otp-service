package com.shivamai.otp.usage.specification;

import com.shivamai.otp.usage.entity.OtpUsageSummary;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OtpUsageSummarySpecification {

    private OtpUsageSummarySpecification() {
    }

    public static Specification<OtpUsageSummary>
    withFilters(
            String search,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        return (root, query, cb) -> {

            var predicate =
                    cb.conjunction();

            if (search != null
                    && !search.isBlank()) {

                String value =
                        "%" + search.toLowerCase() + "%";

                predicate =
                        cb.and(
                                predicate,
                                cb.like(
                                        cb.lower(
                                                root.get("clientId")
                                        ),
                                        value
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

    public static Specification<OtpUsageSummary>
    withClientFilters(
            String clientId,
            String search,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        return (root, query, cb) -> {

            var predicate =
                    cb.equal(
                            root.get("clientId"),
                            clientId
                    );

            if (search != null
                    && !search.isBlank()) {

                String value =
                        "%" + search.toLowerCase() + "%";

                predicate =
                        cb.and(
                                predicate,
                                cb.like(
                                        cb.lower(
                                                root.get("clientId")
                                        ),
                                        value
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