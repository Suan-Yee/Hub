package com.example.demo.application.usecase;

import com.example.demo.dto.NotificationDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface NotificationService {

    void notifyUser(String staffId, String message);
    Notification save(Notification notification);
    Notification changeStatus(Long notiId);
    void createNotification(Post post, User user);
    void updateOrCreateNotificationPost(Post post, User user, boolean show);
    void createNotificationForComment(Post post,Comment comment,User user,boolean show);
    Page<NotificationDto> fetchAllNotificationByRecipient(int page, int size, Sort sort);
    void deleteNotificationByCommentId(Long commentId);
    Long totalNotification();
    Notification findByCommentId(Long commentId);
    boolean changeStatusToRead(Long notiId);
    void deleteOldNotifications();
    void deleteAllNotification();
    void createNotificationForMention(String message,Post post,Comment comment,User user,boolean show);
    void createNotificationForTag(String message, Post post,User user, boolean show);
    void createNotificationForReplyComment(Post post,Comment comment,boolean show);
    List<Notification> findByUserId(Long userId);
    void deleteTagNotification(Long userId,Long postId);
    void deleteMentionNotification(Long userId,Long postId);
}
