package com.shivamai.otp.usage.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_usage_summary",
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_usage_client_date",
                        columnNames = {"clientId", "usageDate"})},
        indexes = {@Index(name = "idx_usage_client_id",
                        columnList = "clientId"),
                @Index(name = "idx_usage_date",
                        columnList = "usageDate")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpUsageSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 120
    )
    private String clientId;

    @Column(nullable = false)
    private LocalDate usageDate;

    @Column(nullable = false)
    private int otpRequests;

    @Column(nullable = false)
    private int otpVerified;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
    }
}