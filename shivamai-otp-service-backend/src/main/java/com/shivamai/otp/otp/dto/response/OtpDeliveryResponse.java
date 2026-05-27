package com.shivamai.otp.otp.dto.response;

import com.shivamai.otp.otp.enums.OtpChannelType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpDeliveryResponse {

    private OtpChannelType channel;
    private int expirySeconds;
    private Long requestId;
}