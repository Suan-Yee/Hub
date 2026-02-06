package com.example.demo.utils;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

public class TimeFormatter {

    public static String formatTimeAgo(LocalDateTime createDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createDate, now);

        long seconds = duration.getSeconds();
        return timeDate(seconds);
    }

    public static String formatTimeAgo(OffsetDateTime createDate) {
        OffsetDateTime now = OffsetDateTime.now();
        Duration duration = Duration.between(createDate, now);

        long seconds = duration.getSeconds();
        return timeDate(seconds);
    }

    public static String formatTime(Date createDate) {
        Date now = new Date();
        long durationMillis = now.getTime() - createDate.getTime();

        long seconds = durationMillis / 1000;
        return timeDate(seconds);
    }

    private static String timeDate(long seconds){

        if (seconds < 60) {
            return "just now";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours ago";
        } else if (seconds < 172800) {
            return "yesterday";
        } else if (seconds < 604800) {  // Less than a week (7 days)
            return (seconds / 86400) + " days ago";
        } else if (seconds < 2592000) { // Less than a month (30 days)
            return (seconds / 604800) + " weeks ago";
        } else if (seconds < 31536000) { // Less than a year (365 days)
            return (seconds / 2592000) + " months ago";
        } else {
            long years = seconds / 31536000;
            return years + " years ago";
        }
    }

}
