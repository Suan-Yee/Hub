package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ChatMessageDto {

    private String content;
    private Date time;
    private Long recipientId;
    private String chatId;
    private Long senderId;


    public ChatMessageDto(String content, Date time, Long recipientId, Long senderId, String chatId) {
        this.content = content;
        this.time = time;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.chatId=chatId;
    }
}
