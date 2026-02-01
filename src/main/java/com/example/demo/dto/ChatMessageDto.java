package com.example.demo.dto;

import java.util.Date;

public record ChatMessageDto(
        String content,
        Date time,
        Long recipientId,
        Long senderId,
        String chatId
) {
    public String getContent() {
        return content;
    }

    public Date getTime() {
        return time;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getChatId() {
        return chatId;
    }
}
