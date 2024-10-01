package com.example.demo.dto;

import com.example.demo.entity.GroupMessage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

import static com.example.demo.utils.TimeFormatter.formatTime;
import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

@Getter
@Setter
public class GroupMessageDto {

    private String content;
    private String time;
    private Long roomId;
    private Long senderId;
    private String name;
    private String userImage;
    private String type;
    private Long messageId;
    private boolean edited;
    private boolean deleted;

    public GroupMessageDto(){

    }

    public GroupMessageDto(GroupMessage groupMessage) {
        this.messageId = groupMessage.getId();
        this.content = groupMessage.getContent();
        this.time = formatTime(groupMessage.getTime());
        this.senderId = groupMessage.getUser().getId();
        this.roomId= groupMessage.getRoomId();
        this.name= groupMessage.getName();
        this.userImage = groupMessage.getUser().getPhoto();
        this.type = groupMessage.getType();
    }
}
