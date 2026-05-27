package com.shivamai.otp.account.entity;

import com.shivamai.otp.account.enums.DeveloperAccountStatus;
import com.shivamai.otp.account.enums.AccountRole;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "developer_accounts",
        indexes = {@Index(name = "idx_developer_identifier",
                        columnList = "identifier"),
                @Index(name = "idx_developer_status",
                        columnList = "status")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String identifier;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountRole accountRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeveloperAccountStatus status;

    private Long emailVerificationRequestId;

    private Long loginOtpRequestId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();
    }
}