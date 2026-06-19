package com.shivamai.otp.otp.service;

import com.shivamai.otp.otp.entity.OtpRequest;
import com.shivamai.otp.otp.enums.OtpStatus;
import com.shivamai.otp.otp.repository.OtpRequestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpAttemptServiceImpl
        implements OtpAttemptService {

    private final OtpRequestRepository repository;

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public int recordFailedAttempt(
            OtpRequest otpRequest
    ) {

        int attempts =
                otpRequest.getAttemptCount() + 1;

        otpRequest.setAttemptCount(
                attempts
        );

        repository.save(
                otpRequest
        );

        return attempts;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void blockOtp(
            OtpRequest otpRequest
    ) {

        otpRequest.setStatus(
                OtpStatus.BLOCKED
        );

        repository.save(
                otpRequest
        );
    }
}