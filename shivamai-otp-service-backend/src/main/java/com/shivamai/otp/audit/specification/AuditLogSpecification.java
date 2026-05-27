package com.shivamai.otp.audit.specification;

import com.shivamai.otp.audit.entity.AuditLog;
import com.shivamai.otp.audit.enums.AuditEventType;

import org.springframework.data.jpa.domain.Specification;

public class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog>
    withFilters(
            String identifier,
            String search,
            AuditEventType eventType,
            Integer statusCode
    ) {

        return (root, query, cb) -> {

            var predicate =
                    cb.equal(
                            root.get("identifier"),
                            identifier
                    );

            if (eventType != null) {

                predicate =
                        cb.and(
                                predicate,
                                cb.equal(
                                        root.get("eventType"),
                                        eventType
                                )
                        );
            }

            if (statusCode != null) {

                predicate =
                        cb.and(
                                predicate,
                                cb.equal(
                                        root.get("status"),
                                        statusCode
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
                                                        root.get("endpoint")
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