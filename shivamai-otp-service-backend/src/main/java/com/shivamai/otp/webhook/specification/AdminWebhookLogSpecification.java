package com.shivamai.otp.webhook.specification;

import com.shivamai.otp.webhook.entity.WebhookLog;
import com.shivamai.otp.webhook.enums.WebhookEventType;
import com.shivamai.otp.webhook.enums.WebhookStatus;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import java.util.List;

public class AdminWebhookLogSpecification {

    private AdminWebhookLogSpecification() {
    }

    public static Specification<WebhookLog>
    withFilters(
            String search,
            List<WebhookStatus> statuses,
            List<WebhookEventType> eventTypes,
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

            if (eventTypes != null
                    && !eventTypes.isEmpty()) {

                predicate =
                        cb.and(
                                predicate,
                                root.get("eventType")
                                        .in(eventTypes)
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
                                                        root.get("clientId")
                                                ),
                                                value
                                        ),

                                        cb.like(
                                                cb.lower(
                                                        root.get("targetUrl")
                                                ),
                                                value
                                        ),

                                        cb.like(
                                                cb.lower(
                                                        root.get("failureReason")
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