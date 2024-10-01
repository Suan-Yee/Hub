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
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service @Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    private final UserService userService;

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
                    .isRead(false)
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

    @Override
    public void createNotificationForComment(Post post,Comment comment, User user, boolean show) {
       Notification notification = Notification.builder()
                        .recipient(post.getUser())
                        .triggeredBy(user)
                        .comment(comment)
                        .post(post)
                        .message(user.getName() + " comment on your post.")
                        .type(NotificationType.COMMENT)
                        .status(true)
                        .isRead(false)
                        .build();
        notificationRepository.save(notification);
    }
    @Override
    public void createNotificationForReplyComment(Post post,Comment comment,boolean show) {
        Notification notification = Notification.builder()
                .recipient(comment.getParentComment().getUser())
                .triggeredBy(comment.getUser())
                .comment(comment)
                .post(post)
                .message(comment.getUser().getName() + " reply to your comment.")
                .type(NotificationType.COMMENT)
                .status(true)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
    @Override
    public void createNotificationForMention(String message, Post post, Comment comment, User user, boolean show) {
        Notification notification = Notification.builder()
                .recipient(user)
                .triggeredBy(comment.getUser())
                .message(message)
                .post(post)
                .comment(comment)
                .type(NotificationType.MENTION)
                .status(true)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
    @Override
    public void createNotificationForTag(String message, Post post,User user, boolean show) {
        Notification notification = Notification.builder()
                .recipient(user)
                .triggeredBy(post.getUser())
                .message(message)
                .post(post)
                .type(NotificationType.TAG)
                .status(true)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public Page<NotificationDto> fetchAllNotificationByRecipient(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page,size,sort);
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long recipientId = userService.findByStaffId(staffId).getId();
        Page<Notification> notifications = notificationRepository.findAllByRecipientId(recipientId,pageable);
        Long totalNoti = totalNotification();
        return notifications.map(notification -> {
            NotificationDto notificationDto = new NotificationDto(notification);
            return notificationDto.setTotalNotification(totalNoti);
        });
    }

    @Override
    public void deleteNotificationByCommentId(Long commentId) {
       Notification notification = notificationRepository.findByCommentId(commentId).orElse(null);
       log.info("Notification Id {}",notification.getId());
       notificationRepository.deleteById(notification.getId());
    }

    @Override
    public Long totalNotification() {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long recipientId = userService.findByStaffId(staffId).getId();
        return notificationRepository.countNotificationByStatusTrue(recipientId);
    }

    @Override
    public Notification findByCommentId(Long commentId) {
        return notificationRepository.findByCommentId(commentId).orElse(null);
    }

    @Override
    public boolean changeStatusToRead(Long notiId) {
        Notification notification = notificationRepository.findById(notiId).orElse(null);
        if(notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }

    @Override
    @Scheduled(fixedRate = 86400000)
    public void deleteOldNotifications() {
        LocalDateTime deleteTime = LocalDateTime.now().minusWeeks(2);
        try {
            notificationRepository.deleteByTimeBefore(deleteTime);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("No old notifications found to delete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void deleteAllNotification() {
        try {
            String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByStaffId(staffId);
            notificationRepository.deleteAllByRecipientId(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByRecipientId(userId);
    }

    @Transactional
    @Override
    public void deleteTagNotification(Long userId,Long postId){
        notificationRepository.deleteTagNotification(postId,userId);
    }

}

