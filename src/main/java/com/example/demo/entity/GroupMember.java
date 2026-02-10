package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "group_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(length = 20)
    private String role = "member"; // 'admin', 'moderator', 'member'
    
    @Column(name = "joined_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime joinedAt;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = OffsetDateTime.now();
    }
    
}
