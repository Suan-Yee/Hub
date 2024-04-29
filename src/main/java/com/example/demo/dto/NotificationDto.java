package com.example.demo.dto;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class NotificationDto {

    private Long id;
    private String message;
    private Long recipientId;
    private Long triggeredById;
    private Long postId;
    private boolean status;
    private String userPhoto;
    private NotificationType type;
    private String time;
    private Long totalNotification;

    public NotificationDto(Notification notification){
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.recipientId = notification.getRecipient().getId();
        this.triggeredById = notification.getTriggeredBy().getId();
        this.postId = notification.getPost().getId();
        this.status = notification.isStatus();
        this.type = notification.getType();
        this.userPhoto = notification.getRecipient().getPhoto();
        this.time = formatTimeAgo(notification.getTime());
    }
    public NotificationDto setTotalNotification(Long totalNotification) {
        this.totalNotification = totalNotification;
        return this; // Return the current object
    }
}
