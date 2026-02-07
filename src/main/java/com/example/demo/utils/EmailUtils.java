package com.example.demo.utils;

import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

    public static String getOtpSendingMessage(String name,String otpCode){
        return "Hello " + name + ",\n\nTo Reset your password use this code.\n\n" +
                otpCode + "\n\nThe Support Team";
    }
}
