package com.shivamai.otp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShivamaiOtpServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShivamaiOtpServiceApplication.class, args);
	}
}
