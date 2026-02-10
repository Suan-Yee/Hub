package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "poll_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;
    
    @Column(name = "vote_count")
    private Integer voteCount = 0;
    
    @ManyToMany
    @JoinTable(
        name = "poll_votes",
        joinColumns = @JoinColumn(name = "poll_option_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> voters = new HashSet<>();
}
