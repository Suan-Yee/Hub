package com.example.demo.services;

import com.example.demo.dto.NotificationDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import java.util.List;

public interface NotificationService {

    void notifyUser(String staffId, String message);
    Notification save(Notification notification);
    Notification changeStatus(Long notiId);
    void createNotification(Post post, User user);
    void updateOrCreateNotificationPost(Post post, User user, boolean show);

//    void updateOrCreateNotificationComment(Comment comment,User receiver,boolean show);
    List<NotificationDto> fetchAllNotificationByRecipient(Long recipientId);

    Long totalNotification();
}
