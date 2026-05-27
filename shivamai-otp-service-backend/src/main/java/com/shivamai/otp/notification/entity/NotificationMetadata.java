package com.shivamai.otp.notification.entity;

public record NotificationMetadata(

        String subject,

        String template,

        String endpoint

) {
}