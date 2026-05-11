package com.shivamai.otp.controller;

import com.shivamai.otp.dtoresponse.ApiResponse;
import com.shivamai.otp.service.MailTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    private final MailTestService mailService;

    @PostMapping("/mail")
    public ApiResponse<String> sendMail(@RequestParam String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        log.info("Test mail triggered for {}", email);

        mailService.sendTestMail(email);

        return new ApiResponse<>(
                "SUCCESS",
                "Mail sent",
                null,
                LocalDateTime.now()
        );
    }
}