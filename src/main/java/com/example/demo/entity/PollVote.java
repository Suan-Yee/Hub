package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "poll_votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollVote {
    
    @EmbeddedId
    private PollVoteId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pollOptionId")
    @JoinColumn(name = "poll_option_id")
    private PollOption pollOption;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
    
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PollVoteId implements Serializable {
        @Column(name = "poll_option_id")
        private Long pollOptionId;
        
        @Column(name = "user_id")
        private Long userId;
    }
}
