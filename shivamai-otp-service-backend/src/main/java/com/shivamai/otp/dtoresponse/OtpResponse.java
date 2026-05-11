package com.shivamai.otp.dtoresponse;

import com.shivamai.otp.enums.OtpChannelType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpResponse {

    private OtpChannelType channel;
    private int expirySeconds;
    private Long requestId;
}