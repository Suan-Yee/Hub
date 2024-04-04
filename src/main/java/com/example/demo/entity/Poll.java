package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "poll")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "name",nullable = false)
    private String name;
    @Column(name = "description")
    private String description;
    @CreatedDate
    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "expired_date", nullable = false)
    private LocalDateTime expiredDate;

    @OneToOne(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private PollOption pollOption;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
