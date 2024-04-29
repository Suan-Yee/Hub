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
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    private LocalDateTime time;

    private boolean status;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @PrePersist
    public void updatetime(){
        this.time = LocalDateTime.now();
    }

//    @OneToOne
//    @JoinColumn(name = "user_mention_id")
//    private UserMention user_mention;
//
//    @OneToOne
//    @JoinColumn(name = "announcement_id")
//    private Announcement announcement;
}
