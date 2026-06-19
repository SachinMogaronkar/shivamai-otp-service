package com.shivamai.otp.otp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OtpTemplateContent {

    private String subject;

    private String template;

    private String title;

    private String subtitle;
}