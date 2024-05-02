package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class GroupMessageDto {

    private String content;
    private Date time;
    private Long roomId;
    private Long senderId;
    private String name;

    public GroupMessageDto() {
    }
    public GroupMessageDto(String content, Date time, Long senderId, Long roomId,String name) {
        this.content = content;
        this.time = time;
        this.senderId = senderId;
        this.roomId=roomId;
        this.name=name;
    }
}
