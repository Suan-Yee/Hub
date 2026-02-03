package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reactions", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_type", "target_id"}),
    indexes = {
        @Index(name = "idx_reactions_target", columnList = "target_type, target_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "target_type", length = 20)
    private String targetType; // 'post' or 'comment'
    
    @Column(name = "target_id")
    private Long targetId;
    
    @Column(name = "reaction_type", length = 20)
    private String reactionType; // 'like', 'love', 'haha', 'sad', 'angry'
    
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
