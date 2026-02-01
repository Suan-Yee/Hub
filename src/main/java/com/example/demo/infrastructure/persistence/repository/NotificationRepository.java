package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    @Query("select n from Notification n where n.post.id = :postId AND n.recipient.id = :recipientId and n.triggeredBy.id = :triggeredId AND n.recipient.turnNoti = false")
    Optional<Notification> findNotificationByPostAndUser(@Param("postId") Long postId,@Param("recipientId") Long recipientId,@Param("triggeredId")Long triggeredId);

    @Query("select n from Notification n where n.recipient.id = :recipientId and n.status = true AND n.recipient.turnNoti = false")
    Page<Notification> findAllByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("select n from Notification n where n.comment.id = :commentId AND n.recipient.id = :recipientId and n.triggeredBy.id = :triggeredId")
    Optional<Notification> findNotificationByCommentAndUser(@Param("commentId") Long commentId,@Param("recipientId") Long recipientId,@Param("triggeredId")Long triggeredId);

    Optional<Notification> findByCommentId(Long commentId);

    @Query("delete from Notification n where n.comment.id =:commentId ")
    void deleteByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.status = true AND n.isRead = false AND n.recipient.turnNoti = false")
    Long countNotificationByStatusTrue(@Param("recipientId")Long recipientId);

    void deleteByTimeBefore(LocalDateTime twoWeeks);

    void deleteAllByRecipientId(Long userId);
    List<Notification> findByRecipientId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.post.id = :postId AND n.recipient.id = :recipientId AND n.type = 'TAG'")
    void deleteTagNotification(@Param("postId") Long postId, @Param("recipientId") Long recipientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.post.id = :postId AND n.recipient.id = :recipientId AND n.type = 'MENTION'")
    void deleteMentionNotification(@Param("postId") Long postId, @Param("recipientId") Long recipientId);

}
