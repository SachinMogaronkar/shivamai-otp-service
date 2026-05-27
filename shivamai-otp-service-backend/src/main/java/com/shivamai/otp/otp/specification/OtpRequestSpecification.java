package com.shivamai.otp.otp.specification;

import com.shivamai.otp.otp.channel.OtpDeliveryChannel;
import com.shivamai.otp.otp.entity.OtpRequest;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;
import com.shivamai.otp.otp.enums.OtpPurpose;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OtpRequestSpecification {

    private OtpRequestSpecification() {
    }

    public static Specification<OtpRequest>
    withFilters(
            String clientId,
            String search,
            List<OtpStatus> statuses,
            List<OtpDeliveryChannel> channels,
            List<OtpType> otpTypes,
            List<OtpPurpose> purposes,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        return (root, query, cb) -> {

            var predicate =
                    cb.equal(
                            root.get("clientId"),
                            clientId
                    );

            if (statuses != null && !statuses.isEmpty()) {

                predicate =
                        cb.and(
                                predicate,
                                root.get("status").in(statuses)
                        );
            }

            if (channels != null
                    && !channels.isEmpty()) {

                predicate =
                        cb.and(
                                predicate,
                                root.get("channel")
                                        .in(channels)
                        );
            }

            if (otpTypes != null
                    && !otpTypes.isEmpty()) {

                predicate =
                        cb.and(
                                predicate,
                                root.get("otpType")
                                        .in(otpTypes)
                        );
            }

            if (purposes != null
                    && !purposes.isEmpty()) {

                predicate =
                        cb.and(
                                predicate,
                                root.get("purpose")
                                        .in(purposes)
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
                                                        root.get("identifier")
                                                ),
                                                value
                                        ),

                                        cb.like(
                                                cb.lower(
                                                        root.get("applicationName")
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