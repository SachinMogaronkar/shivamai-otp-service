package com.shivamai.otp.dtorequest;

import lombok.Builder;
import lombok.Getter;

import org.thymeleaf.context.Context;

@Getter
@Builder
public class EmailRequest {

    private String to;
    private String subject;
    private String template;
    private Context context;
}