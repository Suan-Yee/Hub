package com.example.demo.entity;

import com.example.demo.enumeration.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String message;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "triggeredByUserId", nullable = false)
    private User triggeredBy;

    @ManyToOne
    @JoinColumn(name = "postId")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "commentId")
    private Comment comment;

    @CreatedDate
    private LocalDateTime time;

    private boolean status;
    private boolean isRead;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

}

//    @OneToOne
//    @JoinColumn(name = "user_mention_id")
//    private UserMention user_mention;
//
//    @OneToOne
//    @JoinColumn(name = "announcement_id")
//    private Announcement announcement;
