package com.shivamai.otp.common.security;

public interface ApplicationSessionInvalidationService {

    void invalidateApplicationSessions(
            String clientId
    );
}