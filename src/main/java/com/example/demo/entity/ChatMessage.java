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
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chat_message_chat_id_time", columnList = "chatId, time"),
        @Index(name = "idx_chat_message_room_time", columnList = "room_id, time")
})
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

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;
}