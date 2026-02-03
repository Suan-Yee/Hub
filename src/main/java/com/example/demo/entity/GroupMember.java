package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {
    
    @EmbeddedId
    private GroupMemberId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
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
    
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class GroupMemberId implements Serializable {
        @Column(name = "group_id")
        private Long groupId;
        
        @Column(name = "user_id")
        private Long userId;
    }
}
