package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "invitation")
@EntityListeners(AuditingEntityListener.class)
public class UserInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private Long recipientId;
    private boolean isInvited;
    private boolean isRemoved;
    private boolean isAccepted;
    private boolean isJoined;

    private Long requestId;

    @CreatedDate
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "communityId")
    private Group group;
}
