package com.example.demo.utils;


import java.time.Duration;
import java.time.LocalDateTime;

public class TimeFormatter {

    public static String formatTimeAgo(LocalDateTime createDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createDate, now);

        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return "just now";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours ago";
        } else if (seconds < 172800) {
            return "yesterday";
        } else if (seconds < 31536000) {
            return (seconds / 86400) + " days ago";
        } else {
            long years = seconds / 31536000;
            return years + " years ago";
        }
    }
}
