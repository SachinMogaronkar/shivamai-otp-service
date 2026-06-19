package com.shivamai.otp.application.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.shivamai.otp.account.entity.DeveloperAccount;
import com.shivamai.otp.application.enums.ApplicationStatus;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "developer_applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_developer_application_name",
                        columnNames = {
                                "developerId",
                                "applicationName"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_developer_application_client_id",
                        columnList = "clientId"
                ),
                @Index(
                        name = "idx_developer_application_developer",
                        columnList = "developerId"
                ),
                @Index(
                        name = "idx_developer_application_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_app_created_at",
                        columnList = "createdAt"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "developer_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_application_developer")
    )
    private DeveloperAccount developer;

    @Column(nullable = false, updatable = false, length = 120)
    private String applicationName;

    @Column(nullable = false, unique = true, length = 64, updatable = false)
    private String clientId;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String clientSecretHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ApplicationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime secretRotatedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 500, nullable = true)
    private String webhookUrl;

    @Column(length = 128)
    private String webhookSecret;

    @PrePersist
    public void prePersist() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;

        this.updatedAt = now;

        this.secretRotatedAt = now;

        if (this.status == null) {

            this.status = ApplicationStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {

        this.updatedAt = LocalDateTime.now();
    }
}