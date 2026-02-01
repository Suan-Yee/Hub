package com.example.demo.dto;

import lombok.Builder;

@Builder
public record NotificationChatRoomDto(
        Long id,
        String sender,
        String content
) {
    public Long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
}
