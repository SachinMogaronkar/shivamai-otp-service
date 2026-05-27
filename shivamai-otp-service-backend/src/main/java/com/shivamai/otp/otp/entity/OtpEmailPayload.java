package com.shivamai.otp.otp.entity;

import lombok.Builder;
import lombok.Getter;
import org.thymeleaf.context.Context;

@Getter
@Builder
public class OtpEmailPayload {

    private String to;

    private String subject;

    private String template;

    private Context context;
}
