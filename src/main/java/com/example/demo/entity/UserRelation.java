package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_relations", indexes = {
    @Index(name = "idx_user_relations_follower", columnList = "follower_id"),
    @Index(name = "idx_user_relations_following", columnList = "following_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRelation {
    
    @EmbeddedId
    private UserRelationId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private User follower;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followingId")
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
    
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserRelationId implements Serializable {
        @Column(name = "follower_id")
        private Long followerId;
        
        @Column(name = "following_id")
        private Long followingId;
    }
}
