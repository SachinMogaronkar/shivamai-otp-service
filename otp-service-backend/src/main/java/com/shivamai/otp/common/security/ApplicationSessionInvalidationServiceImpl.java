package com.shivamai.otp.common.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplicationSessionInvalidationServiceImpl
        implements ApplicationSessionInvalidationService {

    @Override
    public void invalidateApplicationSessions(
            String clientId
    ) {

        log.info(
                "Application session invalidation placeholder clientId={}",
                clientId
        );
    }
}