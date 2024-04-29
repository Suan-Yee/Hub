package com.example.demo.services.impl;

import com.example.demo.dto.NotificationDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enumeration.NotificationType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;


    @Override
    public void notifyUser(String staffId, String message) {
        messagingTemplate.convertAndSendToUser(staffId, "/queue/notifications", message);
//        messagingTemplate.convertAndSend("/queue/notifications/" + userId.toString(),message);
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public Notification changeStatus(Long notiId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notiId);

        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setStatus(!notification.isStatus());
            return notificationRepository.save(notification);
        } else {
            throw new ApiException("Notification not found with ID: " + notiId);
        }
    }
    @Override
    public void createNotification(Post post, User user) {
        if (!Objects.equals(user.getId(), post.getUser().getId())) {
            String message = user.getName() + " liked your post.";
            Notification notification = Notification.builder()
                    .recipient(post.getUser())
                    .triggeredBy(user)
                    .message(message)
                    .status(true)
                    .build();
            notificationRepository.save(notification);
        }
    }
    @Override
    public void updateOrCreateNotificationPost(Post post, User user, boolean show) {
        Notification notification = notificationRepository.findNotificationByPostAndUser(post.getId(),post.getUser().getId(),user.getId())
                .orElseGet(() -> Notification.builder()
                        .recipient(post.getUser())
                        .triggeredBy(user)
                        .post(post)
                        .message(user.getName() + " liked your post.")
                        .type(NotificationType.LIKE)
                        .build());
        notification.setStatus(show);
        notificationRepository.save(notification);
    }

//    @Override
//    public void updateOrCreateNotificationComment(Comment comment, User user, boolean show) {
//        Notification notification = notificationRepository.findNotificationByCommentAndUser(comment.getId(),comment.getUser().getId(),user.getId())
//                .orElseGet(() -> Notification.builder()
//                        .recipient(comment.getUser())
//                        .triggeredBy(user)
//                        .comment(comment)
//                        .message(user.getName() + " liked your post.")
//                        .type(NotificationType.COMMENT)
//                        .build());
//        notification.setStatus(show);
//        notificationRepository.save(notification);
//    }

    @Override
    public List<NotificationDto> fetchAllNotificationByRecipient(Long recipientId) {
        List<Notification> notifications = notificationRepository.getAllByRecipientId(recipientId)
                .orElseGet(Collections::emptyList);
        Long totalNoti = notificationRepository.countNotificationByStatusTrue();
        return notifications.stream().map(NotificationDto::new).map(noti -> noti.setTotalNotification(totalNoti)).collect(Collectors.toList());
    }

    @Override
    public Long totalNotification() {
        return notificationRepository.countNotificationByStatusTrue();
    }
}

