package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String content;
    private Date time;
    private Long recipientId;
    private String chatId;
    @Transient
    private Long senderId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User receiver;
}
