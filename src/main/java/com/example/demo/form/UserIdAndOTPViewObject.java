package com.example.demo.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdAndOTPViewObject {
    private long userId;
    private String otpCode;
}
