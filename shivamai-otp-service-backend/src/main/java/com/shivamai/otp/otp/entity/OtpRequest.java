package com.shivamai.otp.otp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.shivamai.otp.account.enums.AccountRole;

import com.shivamai.otp.otp.enums.OtpChannelType;
import com.shivamai.otp.otp.enums.OtpPurpose;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.enums.OtpType;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "otp_requests",
        indexes = {

                @Index(
                        name = "idx_otp_identifier",
                        columnList = "identifier"
                ),

                @Index(
                        name = "idx_otp_status",
                        columnList = "status"
                ),

                @Index(
                        name = "idx_otp_expiry",
                        columnList = "expires_at"
                ),

                @Index(
                        name = "idx_otp_request_lookup",
                        columnList =
                                "identifier,otp_type,status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRequest {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 150
    )
    private String identifier;

    @Column(
            name = "display_name",
            length = 120
    )
    private String displayName;

    @Column(
            name = "application_name",
            length = 120
    )
    private String applicationName;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private OtpPurpose purpose;

    @JsonIgnore
    @Column(
            nullable = false,
            length = 255
    )
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(
            length = 30
    )
    private OtpChannelType channel;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private OtpStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private OtpType otpType;

    @Enumerated(EnumType.STRING)
    @Column(
            length = 40
    )
    private AccountRole accountRole;

    @Builder.Default
    @Column(
            nullable = false
    )
    private int attemptCount =
            0;

    @Column(
            nullable = false,
            updatable = false,
            name = "created_at"
    )
    private LocalDateTime createdAt;

    @Column(
            nullable = false,
            name = "expires_at"
    )
    private LocalDateTime expiresAt;

    @Column(
            name = "verified_at"
    )
    private LocalDateTime verifiedAt;

    @Column(
            name = "last_resend_at"
    )
    private LocalDateTime lastResendAt;

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {

            createdAt =
                    LocalDateTime.now();
        }
    }
}