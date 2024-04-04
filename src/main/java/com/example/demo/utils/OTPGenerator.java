package com.example.demo.utils;

import com.example.demo.services.OtpService;
import lombok.RequiredArgsConstructor;

import java.util.Random;

@RequiredArgsConstructor
public class OTPGenerator {

    private final OtpService otpService;

    public static String otpCode(){

        int length = 6;
        StringBuilder otp = new StringBuilder();
        Random rand = new Random();
        for(int i =0; i < length ; i++){
            otp.append(rand.nextInt(6));
        }
        return otp.toString();
    }

}
