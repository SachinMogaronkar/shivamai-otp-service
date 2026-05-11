package com.shivamai.otp.entity;

import com.shivamai.otp.enums.AppStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "developer_apps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long developerId;

    private String appName;

    @Column(unique = true)
    private String clientId;

    private String clientSecret;

    @Enumerated(EnumType.STRING)
    private AppStatus status;

    private LocalDateTime createdAt;

    private String webhookUrl;
}