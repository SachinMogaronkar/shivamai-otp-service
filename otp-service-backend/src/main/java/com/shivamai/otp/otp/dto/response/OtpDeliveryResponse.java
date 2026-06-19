package com.shivamai.otp.otp.dto.response;

import com.shivamai.otp.otp.enums.OtpChannelType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpDeliveryResponse {

    private OtpChannelType channel;

    private Long requestId;

    private LocalDateTime expiresAt;

    private long remainingSeconds;
}