package com.shivamai.otp.util;

import java.security.MessageDigest;

public class OtpHashUtil {

    public static String hash(String otp) {

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = md.digest(otp.getBytes());

            StringBuilder hex = new StringBuilder();

            for(byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}