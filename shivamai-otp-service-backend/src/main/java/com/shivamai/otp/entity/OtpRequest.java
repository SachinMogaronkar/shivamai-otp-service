package com.shivamai.otp.entity;

import com.shivamai.otp.enums.OtpChannelType;
import com.shivamai.otp.enums.OtpStatus;
import com.shivamai.otp.enums.OtpType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    private OtpChannelType channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attemptCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType otpType;

    private LocalDateTime verifiedAt;

    @Column
    private LocalDateTime lastResendAt;
}