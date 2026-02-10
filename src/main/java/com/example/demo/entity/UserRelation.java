package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_relations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}),
    indexes = {
    @Index(name = "idx_user_relations_follower", columnList = "follower_id"),
    @Index(name = "idx_user_relations_following", columnList = "following_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRelation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User following;
    
    @Column(length = 20)
    private String status = "accepted"; // 'pending', 'accepted'
    
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
    
}
