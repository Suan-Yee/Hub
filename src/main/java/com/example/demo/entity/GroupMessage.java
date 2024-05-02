package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "group_message")
public class GroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String content;

    private Date time;
    @Transient
    private Long senderId;
    private String name;
    @Transient
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
