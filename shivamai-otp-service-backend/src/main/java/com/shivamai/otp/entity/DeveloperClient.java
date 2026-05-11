package com.shivamai.otp.entity;

import com.shivamai.otp.enums.ClientStatus;
import com.shivamai.otp.enums.Role;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "developer_clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String identifier;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    private boolean emailVerified;

    private Long emailVerificationRequestId;

    private Long loginOtpRequestId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}