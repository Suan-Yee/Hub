package com.example.demo.dto;

import com.example.demo.entity.Notification;
import com.example.demo.enumeration.NotificationType;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

public record NotificationDto(
        Long id,
        String message,
        Long recipientId,
        Long triggeredById,
        Long postId,
        boolean status,
        String userPhoto,
        NotificationType type,
        String time,
        Long totalNotification,
        boolean isRead,
        Long commentId,
        Long rootCommentId
) {
    public NotificationDto(Notification notification) {
        this(
                notification.getId(),
                notification.getMessage(),
                notification.getRecipient().getId(),
                notification.getTriggeredBy().getId(),
                notification.getPost().getId(),
                notification.isStatus(),
                notification.getTriggeredBy().getPhoto(),
                notification.getType(),
                formatTimeAgo(notification.getTime()),
                null,
                notification.isRead(),
                notification.getComment() != null ? notification.getComment().getId() : null,
                notification.getComment() != null && notification.getComment().getRootComment() != null
                        ? notification.getComment().getRootComment().getId()
                        : null
        );
    }

    public NotificationDto withTotalNotification(Long value) {
        return new NotificationDto(id, message, recipientId, triggeredById, postId, status, userPhoto, type, time,
                value, isRead, commentId, rootCommentId);
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public Long getTriggeredById() {
        return triggeredById;
    }

    public Long getPostId() {
        return postId;
    }

    public boolean isStatus() {
        return status;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public Long getTotalNotification() {
        return totalNotification;
    }

    public boolean isRead() {
        return isRead;
    }

    public Long getCommentId() {
        return commentId;
    }

    public Long getRootCommentId() {
        return rootCommentId;
    }
}
