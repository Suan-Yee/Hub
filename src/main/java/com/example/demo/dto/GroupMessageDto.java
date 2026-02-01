package com.example.demo.dto;

import com.example.demo.entity.GroupMessage;

import static com.example.demo.utils.TimeFormatter.formatTime;

public record GroupMessageDto(
        String content,
        String time,
        Long roomId,
        Long senderId,
        String name,
        String userImage,
        String type,
        Long messageId,
        boolean edited,
        boolean deleted
) {
    public GroupMessageDto() {
        this(null, null, null, null, null, null, null, null, false, false);
    }

    public GroupMessageDto(GroupMessage groupMessage) {
        this(
                groupMessage.getContent(),
                formatTime(groupMessage.getTime()),
                groupMessage.getRoomId(),
                groupMessage.getUser().getId(),
                groupMessage.getName(),
                groupMessage.getUser().getPhoto(),
                groupMessage.getType(),
                groupMessage.getId(),
                false,
                false
        );
    }

    public GroupMessageDto withContent(String value) {
        return new GroupMessageDto(value, time, roomId, senderId, name, userImage, type, messageId, edited, deleted);
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getName() {
        return name;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getType() {
        return type;
    }

    public Long getMessageId() {
        return messageId;
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
