package com.example.demo.repository;

import com.example.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    @Query("select n from Notification n where n.post.id = :postId AND n.recipient.id = :recipientId and n.triggeredBy.id = :triggeredId")
    Optional<Notification> findNotificationByPostAndUser(@Param("postId") Long postId,@Param("recipientId") Long recipientId,@Param("triggeredId")Long triggeredId);

    @Query("select n from Notification n where n.recipient.id = :recipientId and n.status = true")
    Optional<List<Notification>> getAllByRecipientId(@Param("recipientId") Long recipientId);

//    @Query("select n from Notification n where n.comment.id = :commentId AND n.recipient.id = :recipientId and n.triggeredBy.id = :triggeredId")
//    Optional<Notification> findNotificationByCommentAndUser(@Param("commentId") Long commentId,@Param("recipientId") Long recipientId,@Param("triggeredId")Long triggeredId);

    Long countNotificationByStatusTrue();
}
