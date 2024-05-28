package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "invitation")
public class UserInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;
    private Long recipientId;
    private boolean isInvited;
    private boolean isRemoved;
    private boolean isAccepted;
    private Date date;

    @ManyToOne
    @JoinColumn(name = "communityId")
    private Group group;
}
